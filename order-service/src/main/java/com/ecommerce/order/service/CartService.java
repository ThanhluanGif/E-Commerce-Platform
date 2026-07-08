package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItemResponse;
import java.util.List;

public interface CartService {
    void addToCart(String userId, Long variantId, Integer quantity);
    List<CartItemResponse> getCart(String userId);
    void removeFromCart(String userId, Long variantId);
}
