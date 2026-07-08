package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.CartService;
import com.ecommerce.order.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderId;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final RedissonClient redissonClient;
    private final CartService cartService;
    private final CheckoutTxHelper checkoutTxHelper;
    private final OrderRepository orderRepository;

    @Override
    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public OrderResponse checkout(String userId, CheckoutRequest request) {
        String lockKey = "lock:checkout:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // Attempt to acquire lock with wait time 3 seconds, lease time 10 seconds
            acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Checkout process was interrupted");
        }

        if (!acquired) {
            log.warn("Checkout lock not acquired for user {}. Duplicate request blocked.", userId);
            throw new AppException(HttpStatus.CONFLICT, "Another checkout request is in progress. Please try again.");
        }

        try {
            // 1. Fetch cart items
            List<CartItemResponse> cartItems = cartService.getCart(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart");
            }

            // 2. Delegate to transactional helper to save order & clear cart
            return checkoutTxHelper.createOrderAndClearCart(userId, cartItems, request);
        } finally {
            // Release the lock if held by current thread
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderResponse completeOrder(Long orderId, LocalDateTime createdAt, String userId, String rolesHeader) {
        Order order = orderRepository.findById(new OrderId(orderId, createdAt))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        // Authorization check: Admin, Staff, or the order Owner
        boolean isAuthorized = false;
        if (rolesHeader != null && !rolesHeader.isBlank()) {
            isAuthorized = java.util.Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role) || "ROLE_STAFF".equalsIgnoreCase(role));
        }
        if (!isAuthorized && userId != null && order.getUserId().toString().equals(userId)) {
            isAuthorized = true;
        }

        if (!isAuthorized) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền hoàn tất đơn hàng này!");
        }

        if (!"CONFIRMED".equalsIgnoreCase(order.getStatus()) && !"SHIPPING".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn hàng phải ở trạng thái CONFIRMED hoặc SHIPPING mới có thể hoàn tất!");
        }

        order.setStatus("COMPLETED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order ID: {} successfully completed by user: {}", orderId, userId);

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
