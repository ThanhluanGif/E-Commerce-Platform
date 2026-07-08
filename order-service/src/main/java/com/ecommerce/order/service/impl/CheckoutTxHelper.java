package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.client.ProductVariantClient;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutTxHelper {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StringRedisTemplate redisTemplate;
    private final ProductVariantClient productVariantClient;
    private final InventoryService inventoryService;

    @Transactional
    public OrderResponse createOrderAndClearCart(String userId, List<CartItemResponse> cartItems, CheckoutRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Sort items by variantId to prevent deadlocks on database locks
        cartItems.sort((a, b) -> a.getVariantId().compareTo(b.getVariantId()));

        for (CartItemResponse item : cartItems) {
            // 1. Call Product Service to verify status and trigger optimistic lock on variant version
            com.ecommerce.common.dto.ApiResponse<com.ecommerce.order.dto.ProductVariantResponse> response = 
                    productVariantClient.verifyAndLock(item.getVariantId());
            if (response == null || response.getData() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, 
                        "Product Variant no longer available (id: " + item.getVariantId() + ")");
            }
            com.ecommerce.order.dto.ProductVariantResponse variant = response.getData();
            item.setVariant(variant);

            // 2. Verify stock availability in our warehouses
            inventoryService.verifyStock(item.getVariantId(), item.getQuantity());

            BigDecimal itemPrice = variant.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        BigDecimal shippingFee = BigDecimal.valueOf(30000.00); // flat shipping fee
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.add(shippingFee).subtract(discountAmount);

        // Generate partition-safe composite Order ID and Code
        long orderId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
        String orderCode = "ORD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + 
                String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        LocalDateTime now = LocalDateTime.now();

        // 1. Persist Order
        Order order = Order.builder()
                .id(orderId)
                .createdAt(now)
                .userId(Long.parseLong(userId))
                .orderCode(orderCode)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status("PENDING")
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .updatedAt(now)
                .build();

        orderRepository.save(order);

        // 2. Persist OrderItems & Reserve Stock
        for (CartItemResponse item : cartItems) {
            long orderItemId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
            BigDecimal unitPrice = item.getVariant().getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .id(orderItemId)
                    .orderCreatedAt(now)
                    .orderId(orderId)
                    .productVariantId(item.getVariantId())
                    .productName(item.getVariant().getName())
                    .variantSku(item.getVariant().getSku())
                    .unitPrice(unitPrice)
                    .discountAmount(BigDecimal.ZERO)
                    .quantity(item.getQuantity())
                    .totalPrice(itemTotal)
                    .build();

            orderItemRepository.save(orderItem);

            // 3. Keep stock reservation (reserved_qty) and log RESERVE transaction
            inventoryService.reserveStock(orderId, item.getVariantId(), item.getQuantity());
        }

        // 4. Clear Redis Cart (HDEL the entries or complete key deletion)
        String cartKey = "cart:" + userId;
        redisTemplate.delete(cartKey);

        log.info("Transaction successfully committed. Created order code: {}, cleared cart: {}", orderCode, cartKey);

        return OrderResponse.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status("PENDING")
                .paymentStatus("PENDING")
                .createdAt(now)
                .build();
    }
}
