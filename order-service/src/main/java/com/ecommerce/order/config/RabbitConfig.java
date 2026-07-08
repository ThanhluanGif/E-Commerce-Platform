package com.ecommerce.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "order.exchange";
    public static final String CANCELLED_INVENTORY_QUEUE = "order.cancelled.inventory.queue";
    public static final String CANCELLED_COUPON_QUEUE = "order.cancelled.coupon.queue";
    public static final String ROUTING_KEY_CANCELLED = "order.cancelled";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue cancelledInventoryQueue() {
        return new Queue(CANCELLED_INVENTORY_QUEUE, true);
    }

    @Bean
    public Queue cancelledCouponQueue() {
        return new Queue(CANCELLED_COUPON_QUEUE, true);
    }

    @Bean
    public Binding bindingCancelledInventory(Queue cancelledInventoryQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(cancelledInventoryQueue).to(orderExchange).with(ROUTING_KEY_CANCELLED);
    }

    @Bean
    public Binding bindingCancelledCoupon(Queue cancelledCouponQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(cancelledCouponQueue).to(orderExchange).with(ROUTING_KEY_CANCELLED);
    }
}
