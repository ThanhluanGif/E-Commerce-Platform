package com.ecommerce.order.service;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.client.ProductVariantClient;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.ProductVariantResponse;
import com.ecommerce.order.dto.VariantStatus;
import com.ecommerce.order.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ProductVariantClient productVariantClient;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(redisTemplate, productVariantClient);
    }

    @Test
    void testAddToCart() {
        String userId = "user123";
        Long variantId = 456L;
        Integer quantity = 2;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        cartService.addToCart(userId, variantId, quantity);

        verify(hashOperations, times(1)).increment("cart:" + userId, String.valueOf(variantId), 2L);
    }

    @Test
    void testGetCartSuccess() {
        String userId = "user123";
        String key = "cart:" + userId;

        Map<Object, Object> hashEntries = new HashMap<>();
        hashEntries.put("456", "2");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(key)).thenReturn(hashEntries);

        ProductVariantResponse variantDto = ProductVariantResponse.builder()
                .id(456L)
                .name("Test Variant")
                .price(BigDecimal.valueOf(100.0))
                .status(VariantStatus.ACTIVE)
                .build();

        ApiResponse<ProductVariantResponse> apiResponse = ApiResponse.success("Success", variantDto);
        when(productVariantClient.getVariantById(456L)).thenReturn(apiResponse);

        List<CartItemResponse> result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(456L, result.get(0).getVariantId());
        assertEquals(2, result.get(0).getQuantity());
        assertNotNull(result.get(0).getVariant());
        assertEquals("Test Variant", result.get(0).getVariant().getName());
    }

    @Test
    void testGetCartFeignFailureGracefulHandling() {
        String userId = "user123";
        String key = "cart:" + userId;

        Map<Object, Object> hashEntries = new HashMap<>();
        hashEntries.put("456", "2");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(key)).thenReturn(hashEntries);

        when(productVariantClient.getVariantById(456L)).thenThrow(new RuntimeException("Product service down"));

        List<CartItemResponse> result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(456L, result.get(0).getVariantId());
        assertEquals(2, result.get(0).getQuantity());
        assertNull(result.get(0).getVariant());
    }

    @Test
    void testRemoveFromCart() {
        String userId = "user123";
        Long variantId = 456L;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        cartService.removeFromCart(userId, variantId);

        verify(hashOperations, times(1)).delete("cart:" + userId, String.valueOf(variantId));
    }
}
