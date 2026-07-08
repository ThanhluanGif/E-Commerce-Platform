package com.ecommerce.order.repository;

import com.ecommerce.order.entity.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {
    List<WarehouseStock> findByProductVariantId(Long productVariantId);
    Optional<WarehouseStock> findByWarehouseIdAndProductVariantId(Long warehouseId, Long productVariantId);
}
