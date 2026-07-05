package com.ecommerce.ecommerceapi.payment;

import com.ecommerce.ecommerceapi.entity.Order;
import java.util.Map;

public interface PaymentGateway {
    String createPaymentUrl(Order order, String ipAddress) throws Exception;
    boolean verifyChecksum(Map<String, String> fields);
}
