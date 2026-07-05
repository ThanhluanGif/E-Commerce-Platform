package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {
    List<OrderStatusHistory> findByOrderIdOrderByTimestampAsc(Integer orderId);
}
