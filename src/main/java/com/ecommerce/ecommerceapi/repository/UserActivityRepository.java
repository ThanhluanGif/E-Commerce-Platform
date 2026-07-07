package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.ActivityType;
import com.ecommerce.ecommerceapi.entity.UserActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Integer> {
    
    List<UserActivity> findByUserIdAndActivityTypeOrderByTimestampDesc(
            Integer userId, 
            ActivityType activityType, 
            Pageable pageable
    );

    @Query("SELECT ua.product.id, COUNT(ua.id) as count FROM UserActivity ua " +
           "WHERE ua.activityType = :type AND ua.timestamp >= :since " +
           "GROUP BY ua.product.id " +
           "ORDER BY count DESC")
    List<Object[]> findTopProductsByActivityTypeSince(
            @Param("type") ActivityType type, 
            @Param("since") LocalDateTime since, 
            Pageable pageable
    );
}
