package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Integer id);

    List<Product> findByCategoryId(Integer categoryId);
    Page<Product> findByShopId(Integer shopId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Product> filterByShop(
            @Param("shopId") Integer shopId,
            @Param("name") String name,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:shopId IS NULL OR p.shop.id = :shopId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:active IS NULL OR p.active = :active)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category", "shop"})
    Page<Product> filterProducts(
            @Param("name") String name,
            @Param("categoryId") Integer categoryId,
            @Param("shopId") Integer shopId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.name FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<String> getSearchSuggestions(@Param("q") String q, Pageable pageable);
}
