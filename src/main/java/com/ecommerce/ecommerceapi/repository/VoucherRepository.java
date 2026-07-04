package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByActiveTrue();
    List<Voucher> findByShopIdAndActiveTrue(Integer shopId);
}
