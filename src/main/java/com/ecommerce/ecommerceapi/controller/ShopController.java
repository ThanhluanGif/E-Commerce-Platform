package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ShopDTO;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    // 1. POST: Đăng ký shop mới (Chuyển CUSTOMER -> SELLER)
    @PostMapping("/api/seller/register")
    public ResponseEntity<ApiResponse<ShopDTO>> registerShop(Principal principal, @RequestBody ShopDTO shopDTO) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        Shop shop = shopService.registerShop(userId, shopDTO);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký gian hàng thành công!", shopService.convertToDTO(shop)));
    }

    // 2. GET: Lấy thông tin shop của tôi
    @GetMapping("/api/seller/shop")
    public ResponseEntity<ApiResponse<ShopDTO>> getMyShop(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        Shop shop = shopService.getShopByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin gian hàng thành công!", shopService.convertToDTO(shop)));
    }

    // 3. PUT: Cập nhật thông tin shop của tôi
    @PutMapping("/api/seller/shop")
    public ResponseEntity<ApiResponse<ShopDTO>> updateShop(Principal principal, @RequestBody ShopDTO shopDTO) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        Shop shop = shopService.updateShop(userId, shopDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật gian hàng thành công!", shopService.convertToDTO(shop)));
    }

    // 4. GET: Public API lấy thông tin shop qua slug
    @GetMapping("/api/shops/{slug}")
    public ResponseEntity<ApiResponse<ShopDTO>> getShopBySlug(@PathVariable String slug) {
        Shop shop = shopService.getShopBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin shop thành công!", shopService.convertToDTO(shop)));
    }

    // 5. PUT: Admin phê duyệt shop
    @PutMapping("/api/admin/shops/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopDTO>> approveShop(@PathVariable Integer id) {
        Shop shop = shopService.approveShop(id);
        return ResponseEntity.ok(ApiResponse.success("Đã phê duyệt gian hàng thành công!", shopService.convertToDTO(shop)));
    }

    // 6. PUT: Admin tạm ngưng / mở khóa shop
    @PutMapping("/api/admin/shops/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopDTO>> suspendShop(@PathVariable Integer id, @RequestParam boolean suspend) {
        Shop shop = shopService.suspendShop(id, suspend);
        String msg = suspend ? "Đã tạm ngưng hoạt động gian hàng!" : "Đã mở khóa hoạt động gian hàng!";
        return ResponseEntity.ok(ApiResponse.success(msg, shopService.convertToDTO(shop)));
    }
}
