package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.order.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/inventory")
@RequiredArgsConstructor
@Slf4j
public class InternalInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> releaseStock(@RequestBody OrderCancelledEvent event) {
        log.info("Received internal inventory release request for Order ID: {}", event.getOrderId());
        inventoryService.releaseStock(event.getOrderId());
        return ResponseEntity.ok(ApiResponse.success("Stock released successfully", null));
    }
}
