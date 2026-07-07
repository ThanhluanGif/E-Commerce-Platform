package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.UserActivityRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserActivityService {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Async
    public void logActivity(ActivityType type, Integer productId, Integer duration, String query, String username) {
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
        }

        UserActivity activity = UserActivity.builder()
                .user(user)
                .product(product)
                .activityType(type)
                .duration(duration)
                .searchQuery(query)
                .timestamp(LocalDateTime.now())
                .build();

        userActivityRepository.save(activity);
    }
}
