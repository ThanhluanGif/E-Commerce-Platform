package com.ecommerce.order.repository;

import com.ecommerce.order.entity.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {
    List<WarehouseStock> findByProductVariantId(Long productVariantId);
    Optional<WarehouseStock> findByWarehouseIdAndProductVariantId(Long warehouseId, Long productVariantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.productVariantId = :productVariantId")
    List<WarehouseStock> findByProductVariantIdForUpdate(@Param("productVariantId") Long productVariantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.warehouse.id = :warehouseId AND ws.productVariantId = :productVariantId")
    Optional<WarehouseStock> findByWarehouseIdAndProductVariantIdForUpdate(@Param("warehouseId") Long warehouseId, @Param("productVariantId") Long productVariantId);
}
