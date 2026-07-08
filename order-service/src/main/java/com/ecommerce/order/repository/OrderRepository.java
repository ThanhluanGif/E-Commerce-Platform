package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {
    List<Order> findAllByOrderCode(String orderCode);
    Optional<Order> findById(Long id);
    List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

    default Optional<Order> findByOrderCode(String orderCode) {
        List<Order> orders = findAllByOrderCode(orderCode);
        if (orders.isEmpty()) {
            return Optional.empty();
        }
        if (orders.size() == 1) {
            return Optional.of(orders.get(0));
        }
        if (orderCode != null && orderCode.startsWith("ORD-") && orderCode.length() >= 12) {
            try {
                String dateStr = orderCode.substring(4, 12);
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                for (Order o : orders) {
                    if (o.getCreatedAt().toLocalDate().equals(date)) {
                        return Optional.of(o);
                    }
                }
            } catch (Exception e) {
                // Ignore fallback to return the first one
            }
        }
        return Optional.of(orders.get(0));
    }
}

