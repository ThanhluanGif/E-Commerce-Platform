package com.ecommerce.order.service;

import java.util.Map;

public interface PaymentService {
    String createPaymentUrl(Long orderId, String ipAddress) throws Exception;
    boolean processVNPayIPN(Map<String, String> params);
}
