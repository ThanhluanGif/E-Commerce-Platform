package com.ecommerce.order.scheduler;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderCoupon;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderCouponRepository;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderCancelSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderCouponRepository orderCouponRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderCancelScheduler orderCancelScheduler;

    private Order overdueOrder;

    @BeforeEach
    public void setup() {
        overdueOrder = Order.builder()
                .id(1001L)
                .orderCode("ORD-20260708-999")
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .status("PENDING")
                .paymentStatus("PENDING")
                .build();
    }

    @Test
    public void testScanAndCancelOverdueOrders_Success() {
        when(orderRepository.findByStatusAndCreatedAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueOrder));

        OrderItem orderItem = OrderItem.builder()
                .id(2001L)
                .orderId(1001L)
                .productVariantId(3001L)
                .quantity(2)
                .build();
        when(orderItemRepository.findByOrderId(1001L)).thenReturn(List.of(orderItem));

        OrderCoupon orderCoupon = OrderCoupon.builder()
                .orderId(1001L)
                .couponId(5001L)
                .build();
        when(orderCouponRepository.findByOrderId(1001L)).thenReturn(List.of(orderCoupon));

        orderCancelScheduler.scanAndCancelOverdueOrders();

        verify(orderRepository, times(1)).save(overdueOrder);
        assertEquals("CANCELLED", overdueOrder.getStatus());
        assertEquals("FAILED", overdueOrder.getPaymentStatus());

        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("order.exchange"), eq("order.cancelled"), eventCaptor.capture());

        OrderCancelledEvent sentEvent = eventCaptor.getValue();
        assertEquals(1001L, sentEvent.getOrderId());
        assertEquals("ORD-20260708-999", sentEvent.getOrderCode());
        assertEquals(5001L, sentEvent.getCouponId());
        assertEquals(1, sentEvent.getItems().size());
        assertEquals(3001L, sentEvent.getItems().get(0).getProductVariantId());
        assertEquals(2, sentEvent.getItems().get(0).getQuantity());
    }

    @Test
    public void testScanAndCancelOverdueOrders_NoOverdueOrders() {
        when(orderRepository.findByStatusAndCreatedAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        orderCancelScheduler.scanAndCancelOverdueOrders();

        verify(orderRepository, never()).save(any(Order.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
