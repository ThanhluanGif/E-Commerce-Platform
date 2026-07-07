package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.PointTransaction;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserPoints;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.LoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) {
            throw new com.ecommerce.ecommerceapi.exception.BadRequestException("Yêu cầu cần được xác thực!");
        }
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy người dùng!"))
                .getId();
    }

    @GetMapping("/points")
    public ResponseEntity<ApiResponse<UserPoints>> getMyPoints(Principal principal) {
        Integer userId = getUserId(principal);
        UserPoints userPoints = loyaltyService.getUserPoints(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin điểm thưởng thành công!", userPoints));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PointTransaction>>> getMyPointHistory(Principal principal) {
        Integer userId = getUserId(principal);
        List<PointTransaction> history = loyaltyService.getPointHistory(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử giao dịch điểm thành công!", history));
    }
}
