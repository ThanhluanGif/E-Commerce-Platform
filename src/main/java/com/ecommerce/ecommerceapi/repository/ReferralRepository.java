package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Integer> {
    Optional<Referral> findByRefereeId(Integer refereeId);
    List<Referral> findByReferrerId(Integer referrerId);
    List<Referral> findByReferrerIdAndStatus(Integer referrerId, String status);
}
