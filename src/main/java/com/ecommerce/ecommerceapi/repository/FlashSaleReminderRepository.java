package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.FlashSaleReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleReminderRepository extends JpaRepository<FlashSaleReminder, Integer> {
    Optional<FlashSaleReminder> findByUserIdAndFlashSaleId(Integer userId, Integer flashSaleId);
    List<FlashSaleReminder> findByFlashSaleIdAndNotifiedFalse(Integer flashSaleId);
    List<FlashSaleReminder> findByUserId(Integer userId);
}
