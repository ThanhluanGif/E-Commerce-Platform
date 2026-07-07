package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserId(Integer userId);
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(Integer userId, Integer productId, Integer variantId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantIsNull(Integer userId, Integer productId);
    void deleteByUserId(Integer userId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c.user.id FROM CartItem c WHERE c.updatedAt < :time")
    List<Integer> findUserIdsWithCartItemsOlderThan(@org.springframework.data.repository.query.Param("time") java.time.LocalDateTime time);
}
