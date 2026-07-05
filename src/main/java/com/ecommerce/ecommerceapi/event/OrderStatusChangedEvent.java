package com.ecommerce.ecommerceapi.event;

import com.ecommerce.ecommerceapi.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;

    public OrderStatusChangedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
