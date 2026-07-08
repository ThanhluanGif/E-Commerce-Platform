package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findById(Long id);
    List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);
}

