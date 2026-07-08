package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.CartService;
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

    @Transactional
    public OrderResponse createOrderAndClearCart(String userId, List<CartItemResponse> cartItems, CheckoutRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CartItemResponse item : cartItems) {
            if (item.getVariant() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, 
                        "One or more items in the cart are no longer available (id: " + item.getVariantId() + ")");
            }
            BigDecimal itemPrice = item.getVariant().getPrice();
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

        // 2. Persist OrderItems
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
        }

        // 3. Clear Redis Cart (HDEL the entries or complete key deletion)
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
