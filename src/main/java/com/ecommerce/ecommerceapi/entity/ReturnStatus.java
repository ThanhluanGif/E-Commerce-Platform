package com.ecommerce.ecommerceapi.entity;

public enum ReturnStatus {
    PENDING,    // Buyer created request, waiting for Seller response
    APPROVED,   // Seller approved return request, waiting for Admin refund
    REJECTED,   // Seller rejected return request
    REFUNDED,   // Admin approved and processed refund
    CLOSED      // Request closed/resolved without refund
}
