package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.PasswordChangeRequest;
import com.ecommerce.ecommerceapi.dto.ProfileUpdateRequest;
import com.ecommerce.ecommerceapi.dto.UserDTO;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private Integer getUserId(Principal principal) {
        if (principal == null) {
            throw new com.ecommerce.ecommerceapi.exception.BadRequestException("Yêu cầu cần được xác thực!");
        }
        return userService.getUserByUsername(principal.getName()).getId();
    }

    // 1. GET: Lấy thông tin cá nhân hiện tại
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(Principal principal) {
        Integer userId = getUserId(principal);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân thành công!", userService.convertToDTO(user)));
    }

    // 2. PUT: Cập nhật thông tin cá nhân
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        User updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công!", userService.convertToDTO(updatedUser)));
    }

    // 3. PUT: Thay đổi mật khẩu
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Thay đổi mật khẩu thành công!"));
    }
}
