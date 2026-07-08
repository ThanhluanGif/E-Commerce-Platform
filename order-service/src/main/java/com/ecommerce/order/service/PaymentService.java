package com.ecommerce.order.service;

import java.util.Map;

import java.math.BigDecimal;

public interface PaymentService {
    String createPaymentUrl(Long orderId, String ipAddress) throws Exception;
    boolean processVNPayIPN(Map<String, String> params);
    boolean refundPayment(Long orderId, BigDecimal amount);
}

