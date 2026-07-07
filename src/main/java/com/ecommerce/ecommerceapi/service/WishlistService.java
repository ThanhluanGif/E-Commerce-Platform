package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.Wishlist;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public Wishlist addToWishlist(Integer userId, Integer productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));

        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Sản phẩm đã có trong danh sách yêu thích!");
        }

        Wishlist w = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        return wishlistRepository.save(w);
    }

    public void removeFromWishlist(Integer userId, Integer productId) {
        Wishlist w = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong danh sách yêu thích!"));
        wishlistRepository.delete(w);
    }

    public List<Product> getMyWishlist(Integer userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(Wishlist::getProduct)
                .collect(Collectors.toList());
    }

    public boolean isWishlist(Integer userId, Integer productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    public List<Object[]> getWishlistItemsInFlashSale(Integer userId) {
        return wishlistRepository.findWishlistItemsInActiveFlashSale(userId, java.time.LocalDateTime.now());
    }
}
