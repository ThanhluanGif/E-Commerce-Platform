package com.ecommerce.order.service.impl;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.client.ProductVariantClient;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.ProductVariantResponse;
import com.ecommerce.order.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductVariantClient productVariantClient;

    private static final String CART_KEY_PREFIX = "cart:";

    @Override
    public void addToCart(String userId, Long variantId, Integer quantity) {
        String key = CART_KEY_PREFIX + userId;
        String field = String.valueOf(variantId);
        
        log.info("Adding variant {} with quantity {} to cart of user {}", variantId, quantity, userId);
        
        // Execute HINCRBY
        redisTemplate.opsForHash().increment(key, field, quantity.longValue());
    }

    @Override
    public List<CartItemResponse> getCart(String userId) {
        String key = CART_KEY_PREFIX + userId;
        log.info("Retrieving cart items for user {}", userId);

        // Read all entries in the Redis Hash
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartItemResponse> cartItems = new ArrayList<>();

        if (entries == null || entries.isEmpty()) {
            return cartItems;
        }

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                Long variantId = Long.parseLong(entry.getKey().toString());
                Integer quantity = Integer.parseInt(entry.getValue().toString());

                ProductVariantResponse variantResponse = null;
                try {
                    ApiResponse<ProductVariantResponse> apiResponse = productVariantClient.getVariantById(variantId);
                    if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                        variantResponse = apiResponse.getData();
                    } else {
                        log.warn("Product variant {} details not found or returned unsuccessful response", variantId);
                    }
                } catch (Exception e) {
                    log.error("Error calling Product Service for variant id {}: {}", variantId, e.getMessage());
                }

                cartItems.add(CartItemResponse.builder()
                        .variantId(variantId)
                        .quantity(quantity)
                        .variant(variantResponse)
                        .build());

            } catch (NumberFormatException e) {
                log.error("Invalid numeric formats in Redis Hash for key {}: key={}, value={}", 
                        key, entry.getKey(), entry.getValue());
            }
        }

        return cartItems;
    }

    @Override
    public void removeFromCart(String userId, Long variantId) {
        String key = CART_KEY_PREFIX + userId;
        String field = String.valueOf(variantId);
        
        log.info("Removing variant {} from cart of user {}", variantId, userId);
        
        // Execute HDEL
        redisTemplate.opsForHash().delete(key, field);
    }
}
