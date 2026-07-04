package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ProductDTO;
import com.ecommerce.ecommerceapi.dto.ProductImageDTO;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.Wishlist;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    // 1. GET: Lấy danh sách yêu thích
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getMyWishlist(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        List<ProductDTO> list = wishlistService.getMyWishlist(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu thích thành công!", list));
    }

    // 2. POST: Thêm sản phẩm vào danh sách yêu thích
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(Principal principal, @PathVariable Integer productId) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm sản phẩm vào danh sách yêu thích!"));
    }

    // 3. DELETE: Bỏ yêu thích sản phẩm
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(Principal principal, @PathVariable Integer productId) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi danh sách yêu thích!"));
    }

    // 4. GET: Kiểm tra trạng thái yêu thích của sản phẩm
    @GetMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlistStatus(Principal principal, @PathVariable Integer productId) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success("Chưa đăng nhập!", false));
        }
        boolean status = wishlistService.isWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái yêu thích thành công!", status));
    }

    private ProductDTO convertToDTO(Product product) {
        List<ProductImageDTO> imageDTOs = null;
        if (product.getImages() != null) {
            imageDTOs = product.getImages().stream()
                    .map(img -> ProductImageDTO.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .sortOrder(img.getSortOrder())
                            .build())
                    .toList();
        }

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
                .createdAt(product.getCreatedAt())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getName() : null)
                .images(imageDTOs)
                .build();
    }

    // 5. GET: Lấy danh sách sản phẩm yêu thích đang chạy Flash Sale
    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getWishlistFlashSales(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        List<Object[]> results = wishlistService.getWishlistItemsInFlashSale(userId);
        List<ProductDTO> list = results.stream()
                .map(row -> {
                    Wishlist wishlist = (Wishlist) row[0];
                    com.ecommerce.ecommerceapi.entity.FlashSaleItem fsi = (com.ecommerce.ecommerceapi.entity.FlashSaleItem) row[1];
                    ProductDTO dto = convertToDTO(wishlist.getProduct());
                    dto.setIsFlashSale(true);
                    dto.setSalePrice(fsi.getSalePrice()); // Overwrite with Flash Sale price!
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm yêu thích đang Flash Sale thành công!", list));
    }
}
