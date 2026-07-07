package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.CartRecoveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRecoveryLogRepository extends JpaRepository<CartRecoveryLog, Integer> {
    Optional<CartRecoveryLog> findByUserId(Integer userId);
}
