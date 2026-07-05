package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);
    List<PaymentTransaction> findByOrderId(Integer orderId);
}
