package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CartItemRequest request) {
        
        log.info("Received request to add item to cart. User: {}, Variant: {}, Qty: {}", 
                userId, request.getVariantId(), request.getQuantity());
        
        cartService.addToCart(userId, request.getVariantId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Product added to cart successfully", null));
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Received request to retrieve cart for User: {}", userId);
        
        List<CartItemResponse> cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @DeleteMapping("/remove/{variantId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long variantId) {
        
        log.info("Received request to remove item from cart. User: {}, Variant: {}", userId, variantId);
        
        cartService.removeFromCart(userId, variantId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from cart successfully", null));
    }
}
