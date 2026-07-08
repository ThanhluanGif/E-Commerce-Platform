package com.ecommerce.product.listener;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.product.client.OrderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledInventoryListener {

    private final OrderClient orderClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.cancelled.inventory.queue", durable = "true"),
            exchange = @Exchange(value = "order.exchange", type = "topic"),
            key = "order.cancelled"
    ))
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order.cancelled event for inventory release. Order Code: {}", event.getOrderCode());

        try {
            orderClient.releaseStock(event);
            log.info("Successfully executed inventory release callback for Order Code: {}", event.getOrderCode());
        } catch (Exception e) {
            log.error("Failed to release inventory for Order Code: {}", event.getOrderCode(), e);
            throw e; // Rethrow to let RabbitMQ handle retries/DLQ
        }
    }
}
