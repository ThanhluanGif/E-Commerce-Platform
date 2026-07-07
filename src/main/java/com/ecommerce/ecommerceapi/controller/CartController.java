package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.CartItemDTO;
import com.ecommerce.ecommerceapi.dto.CartItemRequest;
import com.ecommerce.ecommerceapi.entity.CartItem;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.ProductVariant;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

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

    // 1. GET: Lấy toàn bộ sản phẩm trong giỏ hàng
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(Principal principal) {
        Integer userId = getUserId(principal);
        List<CartItemDTO> cart = cartService.getCartForUser(userId).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công!", cart));
    }

    // 2. POST: Thêm sản phẩm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemDTO>> addItem(
            @Valid @RequestBody CartItemRequest request,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        CartItem savedItem = cartService.addItemToCart(userId, request.getProductId(), request.getVariantId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm vào giỏ hàng thành công!", convertToDTO(savedItem)));
    }

    // 3. PUT: Cập nhật số lượng sản phẩm trong giỏ
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateQuantity(
            @PathVariable Integer id,
            @RequestParam Integer quantity,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        CartItem updatedItem = cartService.updateItemQuantity(userId, id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng thành công!", convertToDTO(updatedItem)));
    }

    // 4. DELETE: Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable Integer id, Principal principal) {
        Integer userId = getUserId(principal);
        cartService.removeItemFromCart(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công!"));
    }

    // 5. DELETE: Xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        Integer userId = getUserId(principal);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng!"));
    }

    // 6. POST: Đồng bộ giỏ hàng offline vào giỏ hàng online sau khi login
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<Void>> mergeCart(
            @RequestBody List<CartItemDTO> guestItems,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        cartService.mergeCart(userId, guestItems);
        return ResponseEntity.ok(ApiResponse.success("Đồng bộ giỏ hàng thành công!"));
    }

    private CartItemDTO convertToDTO(CartItem item) {
        Product product = item.getProduct();
        java.math.BigDecimal price = product.getPrice();
        java.math.BigDecimal salePrice = product.getSalePrice();
        Integer stock = product.getStockQuantity();
        String imageUrl = product.getImageUrl();

        if (item.getVariant() != null) {
            ProductVariant variant = item.getVariant();
            if (variant.getPrice() != null) price = variant.getPrice();
            if (variant.getSalePrice() != null) salePrice = variant.getSalePrice();
            stock = variant.getStockQuantity();
            if (variant.getImageUrl() != null && !variant.getImageUrl().trim().isEmpty()) {
                imageUrl = variant.getImageUrl();
            }
        }

        return CartItemDTO.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(imageUrl)
                .productPrice(price)
                .productSalePrice(salePrice)
                .productStockQuantity(stock)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                .build();
    }
}
