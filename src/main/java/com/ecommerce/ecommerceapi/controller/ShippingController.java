package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> calculateShippingFee(
            @RequestParam String city,
            @RequestParam(defaultValue = "1000") double weight
    ) {
        List<Map<String, Object>> options = new ArrayList<>();
        
        // Cấu hình phí ship giả lập theo tỉnh thành
        BigDecimal baseFee = BigDecimal.valueOf(30000);
        String cleanCity = city.toLowerCase();
        
        if (cleanCity.contains("hồ chí minh") || cleanCity.contains("hà nội") || cleanCity.contains("đà nẵng")) {
            baseFee = BigDecimal.valueOf(15000);
        } else if (cleanCity.contains("miền tây") || cleanCity.contains("cần thơ")) {
            baseFee = BigDecimal.valueOf(25000);
        }

        // Tăng phí theo cân nặng (ví dụ: mỗi 1kg thêm 5,000đ)
        BigDecimal weightSurcharge = BigDecimal.valueOf(Math.max(0, Math.ceil(weight / 1000.0) - 1) * 5000);
        BigDecimal standardFee = baseFee.add(weightSurcharge);
        BigDecimal fastFee = standardFee.add(BigDecimal.valueOf(15000));
        BigDecimal expressFee = standardFee.add(BigDecimal.valueOf(30000));

        // 1. Giao hàng tiết kiệm (GHTK)
        Map<String, Object> option1 = new HashMap<>();
        option1.put("id", "ghtk");
        option1.put("name", "Giao Hàng Tiết Kiệm");
        option1.put("fee", standardFee);
        option1.put("estimatedDelivery", "3-5 ngày");
        options.add(option1);

        // 2. Giao hàng nhanh (GHN)
        Map<String, Object> option2 = new HashMap<>();
        option2.put("id", "ghn");
        option2.put("name", "Giao Hàng Nhanh");
        option2.put("fee", fastFee);
        option2.put("estimatedDelivery", "1-2 ngày");
        options.add(option2);

        // 3. Hỏa tốc (GrabExpress / Shopee Express Instant)
        if (cleanCity.contains("hồ chí minh") || cleanCity.contains("hà nội")) {
            Map<String, Object> option3 = new HashMap<>();
            option3.put("id", "express");
            option3.put("name", "Giao Hàng Hỏa Tốc");
            option3.put("fee", expressFee);
            option3.put("estimatedDelivery", "Trong ngày (2-4 giờ)");
            options.add(option3);
        }

        return ResponseEntity.ok(ApiResponse.success("Tính phí vận chuyển thành công!", options));
    }
}
