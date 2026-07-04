package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.CartItem;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.ProductVariant;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.CartItemRepository;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.ProductVariantRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    public List<CartItem> getCartForUser(Integer userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItem addItemToCart(Integer userId, Integer productId, Integer variantId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Số lượng sản phẩm phải lớn hơn 0!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));

        ProductVariant variant = null;
        int maxStock = product.getStockQuantity();

        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm!"));
            if (!variant.getProduct().getId().equals(productId)) {
                throw new BadRequestException("Biến thể này không thuộc về sản phẩm đã chọn!");
            }
            maxStock = variant.getStockQuantity();
        }

        if (maxStock < quantity) {
            throw new BadRequestException("Số lượng tồn kho không đủ (Hiện có: " + maxStock + ")!");
        }

        Optional<CartItem> existingItemOpt = (variantId != null)
                ? cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, productId, variantId)
                : cartItemRepository.findByUserIdAndProductIdAndVariantIsNull(userId, productId);

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            if (maxStock < newQuantity) {
                throw new BadRequestException("Không thể thêm số lượng chỉ định. Tổng số lượng vượt quá tồn kho (Hiện có: " + maxStock + ")!");
            }
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .variant(variant)
                    .quantity(quantity)
                    .build();
            return cartItemRepository.save(newItem);
        }
    }

    public CartItem updateItemQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Số lượng sản phẩm phải lớn hơn 0!");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền sửa đổi sản phẩm này trong giỏ hàng!");
        }

        int maxStock = item.getProduct().getStockQuantity();
        if (item.getVariant() != null) {
            maxStock = item.getVariant().getStockQuantity();
        }

        if (maxStock < quantity) {
            throw new BadRequestException("Số lượng tồn kho không đủ (Hiện có: " + maxStock + ")!");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void removeItemFromCart(Integer userId, Integer cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa sản phẩm này khỏi giỏ hàng!");
        }

        cartItemRepository.delete(item);
    }

    public void clearCart(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public void mergeCart(Integer userId, List<com.ecommerce.ecommerceapi.dto.CartItemDTO> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) return;
        for (com.ecommerce.ecommerceapi.dto.CartItemDTO item : guestItems) {
            try {
                addItemToCart(userId, item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                // Ignore items that fail merge (e.g. out of stock or deleted product) so user login succeeds
            }
        }
    }
}
