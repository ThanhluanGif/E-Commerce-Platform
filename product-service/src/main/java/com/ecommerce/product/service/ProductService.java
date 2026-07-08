package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    void deleteProduct(Long id);
    ProductVariantResponse getVariantById(Long id);
    ProductVariantResponse verifyAndLockVariant(Long id);
    org.springframework.data.domain.Page<ProductResponse> searchProducts(
            String keyword,
            Long categoryId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            org.springframework.data.domain.Pageable pageable
    );
}
