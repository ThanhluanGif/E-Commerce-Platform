package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.Notification;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    // 1. GET: Lấy danh sách thông báo của tôi
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        List<Notification> list = notificationService.getMyNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông báo thành công!", list));
    }

    // 2. GET: Lấy số lượng thông báo chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy số lượng thông báo chưa đọc thành công!", count));
    }

    // 3. PUT: Đánh dấu tất cả đã đọc
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAll(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã đọc tất cả thông báo!"));
    }

    // 4. PUT: Đánh dấu 1 thông báo đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> readOne(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Đã đọc thông báo!"));
    }
}
