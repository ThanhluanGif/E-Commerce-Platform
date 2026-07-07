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

    @org.springframework.data.jpa.repository.Query("SELECT w, fsi FROM Wishlist w " +
            "JOIN FlashSaleItem fsi ON w.product.id = fsi.product.id " +
            "JOIN FlashSale fs ON fsi.flashSale.id = fs.id " +
            "WHERE w.user.id = :userId AND fs.active = true AND fs.startTime <= :now AND fs.endTime >= :now")
    List<Object[]> findWishlistItemsInActiveFlashSale(
            @org.springframework.data.repository.query.Param("userId") Integer userId,
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now
    );
}
