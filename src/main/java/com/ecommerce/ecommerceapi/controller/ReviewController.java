package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ReviewDTO;
import com.ecommerce.ecommerceapi.dto.ReviewRequest;
import com.ecommerce.ecommerceapi.entity.Review;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

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

    // 1. POST: Viết đánh giá sản phẩm (yêu cầu mua hàng & đã giao thành công)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(
            @PathVariable Integer productId,
            @Valid @RequestBody ReviewRequest request,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        Review savedReview = reviewService.createReview(userId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Đăng đánh giá thành công!", convertToDTO(savedReview)));
    }

    // 2. GET: Lấy danh sách đánh giá của sản phẩm (Public)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getReviews(@PathVariable Integer productId) {
        List<ReviewDTO> reviews = reviewService.getReviewsForProduct(productId).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công!", reviews));
    }

    // 3. GET: Kiểm tra xem người dùng hiện tại có thể đánh giá sản phẩm không
    @GetMapping("/can-review")
    public ResponseEntity<ApiResponse<Boolean>> canUserReview(
            @PathVariable Integer productId,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.success("Chưa đăng nhập", false));
        }
        try {
            Integer userId = getUserId(principal);
            boolean hasPurchased = reviewService.hasUserPurchasedProduct(userId, productId);
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra thành công", hasPurchased));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("Lỗi kiểm tra", false));
        }
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
