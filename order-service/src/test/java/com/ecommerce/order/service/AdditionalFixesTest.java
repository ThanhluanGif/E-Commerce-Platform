package com.ecommerce.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderId;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.impl.CheckoutServiceImpl;
import com.ecommerce.order.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdditionalFixesTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    @Test
    public void testIdGeneratorUniqueness() throws InterruptedException {
        int threadCount = 20;
        int idsPerThread = 500;
        int totalIds = threadCount * idsPerThread;
        
        Set<Long> generatedIds = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        generatedIds.add(IdGenerator.nextId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(totalIds, generatedIds.size(), "All generated Snowflake IDs must be unique under heavy load");
    }

    @Test
    public void testOrderRepositoryCodeFallback() {
        LocalDateTime timePart1 = LocalDateTime.of(2026, 7, 8, 10, 0, 0);
        LocalDateTime timePart2 = LocalDateTime.of(2026, 7, 9, 10, 0, 0);

        Order order1 = Order.builder()
                .id(1L)
                .createdAt(timePart1)
                .orderCode("ORD-20260708-000001")
                .totalAmount(BigDecimal.TEN)
                .finalAmount(BigDecimal.TEN)
                .shippingFee(BigDecimal.ZERO)
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .createdAt(timePart2)
                .orderCode("ORD-20260708-000001") // simulate collision
                .totalAmount(BigDecimal.TEN)
                .finalAmount(BigDecimal.TEN)
                .shippingFee(BigDecimal.ZERO)
                .build();

        // Standard stubbing for default query
        OrderRepository repo = mock(OrderRepository.class);
        when(repo.findAllByOrderCode("ORD-20260708-000001")).thenReturn(Arrays.asList(order1, order2));

        // Use repository default method directly
        when(repo.findByOrderCode(anyString())).thenCallRealMethod();

        Optional<Order> result = repo.findByOrderCode("ORD-20260708-000001");
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId(), "Must resolve order matching the date embedded in the orderCode");
    }

    @Test
    public void testCompleteOrderSuccess() {
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .id(12345L)
                .createdAt(now)
                .orderCode("ORD-20260708-111111")
                .status("CONFIRMED")
                .userId(999L)
                .totalAmount(BigDecimal.TEN)
                .finalAmount(BigDecimal.TEN)
                .shippingFee(BigDecimal.ZERO)
                .build();

        when(orderRepository.findById(new OrderId(12345L, now))).thenReturn(Optional.of(order));

        // Act & Assert
        OrderResponse response = checkoutService.completeOrder(12345L, now, "999", "ROLE_USER");
        assertEquals("COMPLETED", response.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testCompleteOrderForbidden() {
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .id(12345L)
                .createdAt(now)
                .orderCode("ORD-20260708-111111")
                .status("CONFIRMED")
                .userId(999L)
                .build();

        when(orderRepository.findById(new OrderId(12345L, now))).thenReturn(Optional.of(order));

        // Attempt from other user without ROLE_ADMIN/ROLE_STAFF should fail
        assertThrows(AppException.class, () -> {
            checkoutService.completeOrder(12345L, now, "888", "ROLE_USER");
        });
    }
}
