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

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final RedissonClient redissonClient;
    private final CartService cartService;
    private final CheckoutTxHelper checkoutTxHelper;

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
}
