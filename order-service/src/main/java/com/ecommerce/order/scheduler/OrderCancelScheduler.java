package com.ecommerce.order.scheduler;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderCoupon;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderCouponRepository;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelScheduler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderCouponRepository orderCouponRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 60000) // Runs every minute
    public void scanAndCancelOverdueOrders() {
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(15);
        log.debug("Scanning for pending orders created before {}", thresholdTime);

        // Scan for orders in PENDING status older than 15 minutes
        List<Order> overdueOrders = orderRepository.findByStatusAndCreatedAtBefore("PENDING", thresholdTime);
        if (overdueOrders.isEmpty()) {
            return;
        }

        log.info("Found {} overdue pending orders to cancel.", overdueOrders.size());
        for (Order order : overdueOrders) {
            try {
                cancelOrderTx(order);
            } catch (Exception e) {
                log.error("Failed to cancel order with ID {}", order.getId(), e);
            }
        }
    }

    @Transactional
    public void cancelOrderTx(Order order) {
        log.info("Cancelling overdue order Code: {}, ID: {}", order.getOrderCode(), order.getId());

        // Update status to CANCELLED and payment status to FAILED
        order.setStatus("CANCELLED");
        order.setPaymentStatus("FAILED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Fetch items associated with this order
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderCancelledEvent.Item> eventItems = orderItems.stream()
                .map(item -> OrderCancelledEvent.Item.builder()
                        .productVariantId(item.getProductVariantId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Check if there was a coupon used
        List<OrderCoupon> orderCoupons = orderCouponRepository.findByOrderId(order.getId());
        Long couponId = null;
        if (!orderCoupons.isEmpty()) {
            couponId = orderCoupons.get(0).getCouponId();
        }

        // Construct the compensation/cancellation event
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .couponId(couponId)
                .items(eventItems)
                .build();

        // Publish event to RabbitMQ
        rabbitTemplate.convertAndSend("order.exchange", "order.cancelled", event);
        log.info("Successfully published order.cancelled event to RabbitMQ for order Code: {}", order.getOrderCode());
    }
}
