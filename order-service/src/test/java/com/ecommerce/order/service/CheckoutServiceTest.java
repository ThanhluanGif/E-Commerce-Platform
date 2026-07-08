package com.ecommerce.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductVariantResponse;
import com.ecommerce.order.service.impl.CheckoutServiceImpl;
import com.ecommerce.order.service.impl.CheckoutTxHelper;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @Mock
    private CartService cartService;

    @Mock
    private CheckoutTxHelper checkoutTxHelper;

    @Mock
    private OrderRepository orderRepository;

    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutServiceImpl(redissonClient, cartService, checkoutTxHelper, orderRepository);
    }

    @Test
    void testCheckoutLockAcquisitionFailure() throws InterruptedException {
        String userId = "user123";
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("Alice")
                .recipientPhone("0123456789")
                .shippingAddress("123 Street")
                .paymentMethod("COD")
                .build();

        when(redissonClient.getLock("lock:checkout:" + userId)).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            checkoutService.checkout(userId, request);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Another checkout request is in progress. Please try again.", exception.getMessage());
    }

    @Test
    void testCheckoutEmptyCart() throws InterruptedException {
        String userId = "user123";
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("Alice")
                .recipientPhone("0123456789")
                .shippingAddress("123 Street")
                .paymentMethod("COD")
                .build();

        when(redissonClient.getLock("lock:checkout:" + userId)).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(cartService.getCart(userId)).thenReturn(new ArrayList<>());

        AppException exception = assertThrows(AppException.class, () -> {
            checkoutService.checkout(userId, request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot checkout an empty cart", exception.getMessage());
        verify(lock, times(1)).unlock();
    }

    @Test
    void testCheckoutSuccess() throws InterruptedException {
        String userId = "user123";
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("Alice")
                .recipientPhone("0123456789")
                .shippingAddress("123 Street")
                .paymentMethod("COD")
                .build();

        List<CartItemResponse> cartItems = Collections.singletonList(
                CartItemResponse.builder()
                        .variantId(456L)
                        .quantity(2)
                        .variant(ProductVariantResponse.builder()
                                .id(456L)
                                .price(BigDecimal.valueOf(100.0))
                                .name("Item")
                                .sku("SKU")
                                .build())
                        .build()
        );

        when(redissonClient.getLock("lock:checkout:" + userId)).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(cartService.getCart(userId)).thenReturn(cartItems);

        OrderResponse expectedResponse = OrderResponse.builder()
                .orderId(999L)
                .orderCode("ORD-20260708-999999")
                .build();

        when(checkoutTxHelper.createOrderAndClearCart(userId, cartItems, request)).thenReturn(expectedResponse);

        OrderResponse actualResponse = checkoutService.checkout(userId, request);

        assertNotNull(actualResponse);
        assertEquals("ORD-20260708-999999", actualResponse.getOrderCode());
        verify(lock, times(1)).unlock();
    }
}
