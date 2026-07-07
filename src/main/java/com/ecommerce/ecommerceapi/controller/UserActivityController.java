package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.ActivityType;
import com.ecommerce.ecommerceapi.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/activities")
public class UserActivityController {

    @Autowired
    private UserActivityService userActivityService;

    @PostMapping("/track")
    public ResponseEntity<ApiResponse<String>> trackActivity(
            @RequestParam ActivityType type,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String query,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        userActivityService.logActivity(type, productId, duration, query, username);
        return ResponseEntity.ok(ApiResponse.success("Hoạt động đã được ghi nhận thành công!", "Success"));
    }
}
