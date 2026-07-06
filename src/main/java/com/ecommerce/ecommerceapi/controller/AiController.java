package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    // 1. POST: Tạo mô tả sản phẩm bằng AI
    @PostMapping("/copywriter")
    public ResponseEntity<ApiResponse<String>> generateDescription(
            Principal principal,
            @RequestBody Map<String, Object> body) {
        
        // Chỉ cho phép người dùng đăng nhập (đặc biệt là Seller hoặc Admin) sử dụng công cụ AI này
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        String name = (String) body.get("name");
        String category = (String) body.get("category");
        List<String> keywords = (List<String>) body.getOrDefault("keywords", List.of());

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tên sản phẩm không được trống!"));
        }

        String description = aiService.generateProductDescription(name, category, keywords);
        return ResponseEntity.ok(ApiResponse.success("Tạo mô tả sản phẩm thành công!", description));
    }

    // 2. POST: Tìm sản phẩm bằng hình ảnh
    @PostMapping("/visual-search")
    public ResponseEntity<ApiResponse<List<Product>>> visualSearch(@RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu đường dẫn hình ảnh (imageUrl)!"));
        }

        List<Product> products = aiService.searchByImage(imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm bằng hình ảnh thành công!", products));
    }

    // 3. POST: Trò chuyện với AI Chatbot hỗ trợ khách hàng
    @PostMapping("/chatbot")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatbotQuery(
            Principal principal,
            @RequestBody Map<String, String> body) {
        
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Nội dung tin nhắn trống!"));
        }

        Integer userId = getUserId(principal);
        // Nếu khách vãng lai, gán userId giả lập
        if (userId == null) {
            userId = 0;
        }

        Map<String, Object> chatbotResult = aiService.processChatbotQuery(message, userId);
        return ResponseEntity.ok(ApiResponse.success("Trợ lý ảo phản hồi thành công!", chatbotResult));
    }
}
