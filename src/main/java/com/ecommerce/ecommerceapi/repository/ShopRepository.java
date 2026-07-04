package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Optional<Shop> findByUserId(Integer userId);
    Optional<Shop> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
