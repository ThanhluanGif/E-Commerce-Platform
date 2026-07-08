package com.ecommerce.order.repository;

import com.ecommerce.order.entity.ReturnRequest;
import com.ecommerce.order.entity.ReturnRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, ReturnRequestId> {
    Optional<ReturnRequest> findById(Long id);
}
