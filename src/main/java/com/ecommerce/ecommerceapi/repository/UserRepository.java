package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Integer> {
    // Hàm phục vụ cho việc Đăng nhập (Tìm user bằng username)
    Optional<User> findByUsername(String username);

    // Hàm kiểm tra xem email đã tồn tại khi đăng ký chưa
    boolean existsByEmail(String email);
}
