package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByUserId(Integer userId);
    Optional<Wishlist> findByUserIdAndProductId(Integer userId, Integer productId);
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
}
