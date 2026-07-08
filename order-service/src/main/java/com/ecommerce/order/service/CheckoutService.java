package com.ecommerce.order.service;

import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;

public interface CheckoutService {
    OrderResponse checkout(String userId, CheckoutRequest request);
}
