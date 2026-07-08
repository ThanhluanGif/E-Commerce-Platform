package com.ecommerce.order.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.product-service.url}")
public interface ProductVariantClient {

    @GetMapping("/api/v1/variants/{id}")
    ApiResponse<ProductVariantResponse> getVariantById(@PathVariable("id") Long id);
}
