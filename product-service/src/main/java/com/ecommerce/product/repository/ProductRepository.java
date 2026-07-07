package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByDeletedAtIsNull(Pageable pageable);
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);
    boolean existsBySkuAndDeletedAtIsNull(String sku);
    boolean existsBySlugAndDeletedAtIsNull(String slug);
}
