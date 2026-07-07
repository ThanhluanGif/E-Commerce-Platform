package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Integer> {
    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
