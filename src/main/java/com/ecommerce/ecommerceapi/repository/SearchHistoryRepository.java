package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Integer userId);
    void deleteByUserId(Integer userId);
}
