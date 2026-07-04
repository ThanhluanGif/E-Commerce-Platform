package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Integer> {
    List<FlashSaleItem> findByFlashSaleId(Integer flashSaleId);
    Optional<FlashSaleItem> findByFlashSaleIdAndProductId(Integer flashSaleId, Integer productId);
}
