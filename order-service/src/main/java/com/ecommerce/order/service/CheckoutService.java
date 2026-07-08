package com.ecommerce.order.service;

import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;

import java.time.LocalDateTime;

public interface CheckoutService {
    OrderResponse checkout(String userId, CheckoutRequest request);
    OrderResponse completeOrder(Long orderId, LocalDateTime createdAt, String userId, String rolesHeader);
}
