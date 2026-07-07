package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);
    List<Review> findByUserIdOrderByCreatedAtDesc(Integer userId);
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
}
