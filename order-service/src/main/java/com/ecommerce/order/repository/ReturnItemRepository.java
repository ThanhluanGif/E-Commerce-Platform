package com.ecommerce.order.repository;

import com.ecommerce.order.entity.ReturnItem;
import com.ecommerce.order.entity.ReturnItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, ReturnItemId> {
    List<ReturnItem> findByReturnRequestId(Long returnRequestId);
}
