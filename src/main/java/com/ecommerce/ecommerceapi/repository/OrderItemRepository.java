package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.OrderItem;
import com.ecommerce.ecommerceapi.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    @Query("SELECT oi_other.product FROM OrderItem oi_other " +
           "WHERE oi_other.order.id IN (" +
           "  SELECT oi.order.id FROM OrderItem oi WHERE oi.product.id = :productId" +
           ") AND oi_other.product.id != :productId " +
           "GROUP BY oi_other.product " +
           "ORDER BY COUNT(oi_other.id) DESC")
    List<Product> findFrequentlyBoughtTogether(
            @Param("productId") Integer productId, 
            Pageable pageable
    );
}
