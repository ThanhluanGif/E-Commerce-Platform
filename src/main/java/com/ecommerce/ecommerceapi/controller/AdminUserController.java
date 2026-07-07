package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.UserDTO;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserRole;
import com.ecommerce.ecommerceapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // 1. GET: Lấy danh sách tài khoản người dùng có phân trang (Admin only)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserDTO> users = userService.getAllUsers(pageable)
                .map(userService::convertToDTO);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công!", users));
    }

    // 2. PUT: Thay đổi vai trò người dùng (Admin only)
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserDTO>> changeRole(
            @PathVariable Integer id,
            @RequestParam UserRole role
    ) {
        User updatedUser = userService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò người dùng thành công!", userService.convertToDTO(updatedUser)));
    }
}
