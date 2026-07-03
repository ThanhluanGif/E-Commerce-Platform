package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.ReviewRequest;
import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.ReviewRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Review createReview(Integer userId, Integer productId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));

        // Check if user has already reviewed the product
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi!");
        }

        // Verify if user has purchased the product
        if (!hasUserPurchasedProduct(userId, productId)) {
            throw new BadRequestException("Bạn chỉ có thể đánh giá sản phẩm sau khi đã nhận hàng thành công!");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm!");
        }
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public boolean hasUserPurchasedProduct(Integer userId, Integer productId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Order order : orders) {
            // Check if order is DELIVERED
            if (order.getStatus() == OrderStatus.DELIVERED) {
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProduct().getId().equals(productId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
