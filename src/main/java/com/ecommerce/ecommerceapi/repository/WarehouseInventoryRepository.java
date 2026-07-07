package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Integer> {
    Optional<WarehouseInventory> findByWarehouseIdAndProductVariantId(Integer warehouseId, Integer productVariantId);

    List<WarehouseInventory> findByWarehouseId(Integer warehouseId);

    List<WarehouseInventory> findByProductVariantId(Integer productVariantId);

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.quantity <= wi.inventoryThreshold")
    List<WarehouseInventory> findLowStockInventories();

    @Query("SELECT wi FROM WarehouseInventory wi JOIN wi.productVariant pv JOIN pv.product p WHERE p.shop.id = :shopId AND wi.quantity <= wi.inventoryThreshold")
    List<WarehouseInventory> findLowStockInventoriesByShop(@Param("shopId") Integer shopId);
}
