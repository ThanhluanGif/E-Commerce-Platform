package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CheckoutRequest request) {
        
        log.info("Received checkout request for user: {}, payment: {}", userId, request.getPaymentMethod());
        
        OrderResponse response = checkoutService.checkout(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", response));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable Long id,
            @RequestParam("createdAt") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime createdAt) {
        
        log.info("Received complete order request for ID: {}, user: {}, roles: {}", id, userId, rolesHeader);
        
        OrderResponse response = checkoutService.completeOrder(id, createdAt, userId, rolesHeader);
        return ResponseEntity.ok(ApiResponse.success("Đơn hàng đã được hoàn tất thành công!", response));
    }
}
