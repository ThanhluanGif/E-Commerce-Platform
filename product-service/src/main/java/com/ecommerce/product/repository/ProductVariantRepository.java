package com.ecommerce.product.repository;

import com.ecommerce.product.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByIdAndDeletedAtIsNull(Long id);
    boolean existsBySkuAndDeletedAtIsNull(String sku);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select pv from ProductVariant pv where pv.id = :id and pv.deletedAt is null")
    Optional<ProductVariant> findByIdAndDeletedAtIsNullWithOptimisticLock(@Param("id") Long id);
}
