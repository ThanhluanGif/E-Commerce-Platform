package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ChatConversationDTO;
import com.ecommerce.ecommerceapi.dto.ChatMessageDTO;
import com.ecommerce.ecommerceapi.entity.ChatConversation;
import com.ecommerce.ecommerceapi.entity.ChatMessage;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    // 1. POST: Tạo hoặc lấy cuộc hội thoại với Shop
    @PostMapping("/conversations/{shopId}")
    public ResponseEntity<ApiResponse<ChatConversationDTO>> getOrCreateConversation(
            Principal principal,
            @PathVariable Integer shopId
    ) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        ChatConversation conv = chatService.getOrCreateConversation(userId, shopId);
        return ResponseEntity.ok(ApiResponse.success("Hội thoại sẵn sàng!", chatService.convertToConversationDTO(conv)));
    }

    // 2. GET: Lấy danh sách hội thoại của tôi (Buyer hoặc Seller)
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ChatConversationDTO>>> getConversations(
            Principal principal,
            @RequestParam(defaultValue = "false") boolean isSeller
    ) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        List<ChatConversationDTO> conversations = chatService.getConversations(userId, isSeller);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hội thoại thành công!", conversations));
    }

    // 3. GET: Lấy tin nhắn trong cuộc hội thoại
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(
            Principal principal,
            @PathVariable Integer id
    ) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        List<ChatMessageDTO> messages = chatService.getMessages(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy tin nhắn thành công!", messages));
    }

    // 4. POST: Gửi tin nhắn mới
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
            Principal principal,
            @PathVariable Integer id,
            @RequestBody Map<String, String> requestBody
    ) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        String content = requestBody.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Nội dung tin nhắn trống!"));
        }
        ChatMessage msg = chatService.sendMessage(id, userId, content);
        return ResponseEntity.ok(ApiResponse.success("Gửi tin nhắn thành công!", chatService.convertToMessageDTO(msg)));
    }
}
