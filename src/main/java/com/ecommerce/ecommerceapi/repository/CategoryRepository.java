package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
    // Chỉ cần viết trống như thế này thôi!
    // Kế thừa JpaRepository là  nghiễm nhiên có sẵn các hàm:
    // findAll(), findById(), save(), deleteById()... không cần code thêm gì cả.
}
