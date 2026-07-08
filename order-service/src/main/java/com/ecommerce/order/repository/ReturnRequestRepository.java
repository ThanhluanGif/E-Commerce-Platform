package com.ecommerce.order.repository;

import com.ecommerce.order.entity.ReturnRequest;
import com.ecommerce.order.entity.ReturnRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, ReturnRequestId> {
    Optional<ReturnRequest> findById(Long id);
    List<ReturnRequest> findByOrderId(Long orderId);
}
