package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.impl.CheckoutServiceImpl;
import com.ecommerce.order.service.impl.CheckoutTxHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CheckoutRetryTest {

    @Configuration
    @EnableRetry
    static class TestConfig {
        @Bean
        public RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }

        @Bean
        public CartService cartService() {
            return mock(CartService.class);
        }

        @Bean
        public CheckoutTxHelper checkoutTxHelper() {
            return mock(CheckoutTxHelper.class);
        }

        @Bean
        public CheckoutService checkoutService(RedissonClient redissonClient, CartService cartService, CheckoutTxHelper checkoutTxHelper) {
            return new CheckoutServiceImpl(redissonClient, cartService, checkoutTxHelper);
        }
    }

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CheckoutTxHelper checkoutTxHelper;

    @Autowired
    private RedissonClient redissonClient;

    private RLock mockLock;

    @BeforeEach
    void setUp() throws InterruptedException {
        // Reset mocks since they are Spring Beans and reused between tests
        reset(cartService, checkoutTxHelper, redissonClient);

        mockLock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void testCheckoutRetrySuccessOnThirdAttempt() {
        String userId = "user-retry-1";
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("Bob")
                .recipientPhone("0987654321")
                .shippingAddress("456 Avenue")
                .paymentMethod("COD")
                .build();

        CartItemResponse item = CartItemResponse.builder()
                .variantId(99L)
                .quantity(1)
                .build();

        when(cartService.getCart(userId)).thenReturn(Collections.singletonList(item));

        // Throw optimistic locking exception twice, then succeed on third try
        when(checkoutTxHelper.createOrderAndClearCart(eq(userId), anyList(), eq(request)))
                .thenThrow(new ObjectOptimisticLockingFailureException("variant conflict", new RuntimeException()))
                .thenThrow(new ObjectOptimisticLockingFailureException("variant conflict", new RuntimeException()))
                .thenReturn(OrderResponse.builder().orderId(777L).orderCode("ORD-RETRY").build());

        OrderResponse response = checkoutService.checkout(userId, request);

        assertNotNull(response);
        verify(checkoutTxHelper, times(3)).createOrderAndClearCart(eq(userId), anyList(), eq(request));
    }

    @Test
    void testCheckoutRetryExceededMaxAttempts() {
        String userId = "user-retry-2";
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("Bob")
                .recipientPhone("0987654321")
                .shippingAddress("456 Avenue")
                .paymentMethod("COD")
                .build();

        CartItemResponse item = CartItemResponse.builder()
                .variantId(99L)
                .quantity(1)
                .build();

        when(cartService.getCart(userId)).thenReturn(Collections.singletonList(item));

        // Throw optimistic locking exception indefinitely (fails all 3 retries)
        when(checkoutTxHelper.createOrderAndClearCart(eq(userId), anyList(), eq(request)))
                .thenThrow(new ObjectOptimisticLockingFailureException("variant conflict", new RuntimeException()));

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            checkoutService.checkout(userId, request);
        });

        verify(checkoutTxHelper, times(3)).createOrderAndClearCart(eq(userId), anyList(), eq(request));
    }
}
