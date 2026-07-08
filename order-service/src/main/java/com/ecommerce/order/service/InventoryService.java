package com.ecommerce.order.service;

public interface InventoryService {
    void verifyStock(Long productVariantId, int quantity);
    void reserveStock(Long orderId, Long productVariantId, int quantity);
    void releaseStock(Long orderId);
}

