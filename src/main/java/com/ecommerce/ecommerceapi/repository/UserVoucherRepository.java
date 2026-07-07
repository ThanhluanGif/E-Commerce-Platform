package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    List<UserVoucher> findByUserId(Integer userId);
    Optional<UserVoucher> findByUserIdAndVoucherId(Integer userId, Integer voucherId);
}
