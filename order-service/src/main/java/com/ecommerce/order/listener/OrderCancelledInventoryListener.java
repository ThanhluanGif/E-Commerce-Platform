package com.ecommerce.order.listener;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.order.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledInventoryListener {

    private final InventoryService inventoryService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.cancelled.inventory.queue", durable = "true"),
            exchange = @Exchange(value = "order.exchange", type = "topic"),
            key = "order.cancelled"
    ))
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order.cancelled event for inventory release. Order ID: {}, Order Code: {}", 
                event.getOrderId(), event.getOrderCode());

        try {
            inventoryService.releaseStock(event.getOrderId());
            log.info("Successfully executed local inventory release for Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to release inventory locally for Order ID: {}", event.getOrderId(), e);
            throw e; // Let RabbitMQ handle retries/DLQ
        }
    }
}
