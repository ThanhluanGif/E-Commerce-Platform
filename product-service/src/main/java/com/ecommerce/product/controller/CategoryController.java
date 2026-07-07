package com.ecommerce.product.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success("Category tree retrieved successfully", tree));
    }
}
