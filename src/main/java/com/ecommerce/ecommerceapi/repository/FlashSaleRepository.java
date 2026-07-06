package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {
    List<FlashSale> findByActiveTrue();
    List<FlashSale> findByActiveTrueAndStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);
    List<FlashSale> findByActiveTrueAndStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
