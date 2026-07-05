package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointsRepository extends JpaRepository<UserPoints, Integer> {
    Optional<UserPoints> findByUserId(Integer userId);
}
