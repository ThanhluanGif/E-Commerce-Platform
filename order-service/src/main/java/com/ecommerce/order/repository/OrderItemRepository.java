package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
