package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.SearchHistory;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.SearchHistoryRepository;
import com.ecommerce.ecommerceapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@Transactional
public class SearchController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private UserService userService;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        try {
            return userService.getUserByUsername(principal.getName()).getId();
        } catch (Exception e) {
            return null;
        }
    }

    // 1. GET /api/search/suggestions?q=...
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(@RequestParam String q) {
        List<String> suggestions = productRepository.getSearchSuggestions(q, PageRequest.of(0, 10));
        return ResponseEntity.ok(ApiResponse.success("Lấy gợi ý tìm kiếm thành công!", suggestions));
    }

    // 2. GET /api/search/history
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SearchHistory>>> getHistory(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success("Chưa đăng nhập, không có lịch sử!", new ArrayList<>()));
        }
        List<SearchHistory> history = searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // Giới hạn hiển thị 10 lịch sử tìm kiếm gần nhất
        if (history.size() > 10) {
            history = history.subList(0, 10);
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tìm kiếm thành công!", history));
    }

    // 3. POST /api/search/history
    @PostMapping("/history")
    public ResponseEntity<ApiResponse<SearchHistory>> saveHistory(@RequestParam String query, Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null || query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Bỏ qua lưu lịch sử!", null));
        }

        String cleanQuery = query.trim();
        // Tránh trùng lặp lịch sử liên tiếp
        List<SearchHistory> existing = searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (!existing.isEmpty() && existing.get(0).getQueryText().equalsIgnoreCase(cleanQuery)) {
            return ResponseEntity.ok(ApiResponse.success("Lịch sử đã tồn tại gần nhất!", existing.get(0)));
        }

        // Tạo lịch sử mới
        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .queryText(cleanQuery)
                .build();
        SearchHistory saved = searchHistoryRepository.save(history);
        return ResponseEntity.ok(ApiResponse.success("Lưu lịch sử tìm kiếm thành công!", saved));
    }

    // 4. DELETE /api/search/history
    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> clearHistory(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId != null) {
            searchHistoryRepository.deleteByUserId(userId);
        }
        return ResponseEntity.ok(ApiResponse.success("Xóa lịch sử tìm kiếm thành công!", null));
    }
}
