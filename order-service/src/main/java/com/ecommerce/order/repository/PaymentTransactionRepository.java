package com.ecommerce.order.repository;

import com.ecommerce.order.entity.PaymentTransaction;
import com.ecommerce.order.entity.PaymentTransactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, PaymentTransactionId> {
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);
    List<PaymentTransaction> findByOrderId(Long orderId);
}
