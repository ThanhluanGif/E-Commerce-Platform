package com.ecommerce.product.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.event.OrderCancelledEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "${app.order-service.url}")
public interface OrderClient {

    @PostMapping("/api/v1/internal/inventory/release")
    ApiResponse<Void> releaseStock(@RequestBody OrderCancelledEvent event);
}
