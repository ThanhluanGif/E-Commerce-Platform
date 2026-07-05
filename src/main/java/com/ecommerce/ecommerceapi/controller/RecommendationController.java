package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ProductDTO;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/similar/{productId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getSimilarProducts(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<ProductDTO> dtos = recommendationService.getSimilarProducts(productId, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm tương tự thành công!", dtos));
    }

    @GetMapping("/frequently-bought/{productId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFrequentlyBoughtTogether(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<ProductDTO> dtos = recommendationService.getCollaborativeFilteringRecommendations(productId, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thường mua cùng thành công!", dtos));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getTrendingProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ProductDTO> dtos = recommendationService.getTrendingProducts(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm xu hướng thành công!", dtos));
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRecentlyViewed(
            @RequestParam(defaultValue = "10") int limit,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.success("Chưa đăng nhập, không có sản phẩm đã xem!", List.of()));
        }

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("Không tìm thấy người dùng!", List.of()));
        }

        List<ProductDTO> dtos = recommendationService.getRecentlyViewed(user.getId(), limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm đã xem gần đây thành công!", dtos));
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getName() : null)
                .build();
    }
}
