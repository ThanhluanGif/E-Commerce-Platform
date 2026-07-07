package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ReferralDTO;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    @Autowired
    private ReferralService referralService;

    @Autowired
    private UserRepository userRepository;

    private User getUser(Principal principal) {
        if (principal == null) {
            throw new com.ecommerce.ecommerceapi.exception.BadRequestException("Yêu cầu cần được xác thực!");
        }
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy người dùng!"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferralDTO>>> getMyReferrals(Principal principal) {
        User user = getUser(principal);
        List<ReferralDTO> referrals = referralService.getReferralsForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giới thiệu thành công!", referrals));
    }

    @GetMapping("/code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getMyReferralCode(Principal principal) {
        User user = getUser(principal);
        Map<String, String> result = new HashMap<>();
        result.put("referralCode", user.getReferralCode());
        return ResponseEntity.ok(ApiResponse.success("Lấy mã giới thiệu thành công!", result));
    }
}
