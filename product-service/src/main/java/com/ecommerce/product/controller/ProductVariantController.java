package com.ecommerce.product.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.dto.ProductVariantResponse;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(@PathVariable Long id) {
        ProductVariantResponse response = productService.getVariantById(id);
        return ResponseEntity.ok(ApiResponse.success("Variant retrieved successfully", response));
    }

    @PostMapping("/{id}/verify-and-lock")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> verifyAndLock(@PathVariable Long id) {
        ProductVariantResponse response = productService.verifyAndLockVariant(id);
        return ResponseEntity.ok(ApiResponse.success("Variant verified and locked successfully", response));
    }
}
