package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository  extends CrudRepository<Product,Integer> {
    // Ngoài các hàm cơ bản, bro có thể tự định nghĩa thêm hàm tìm kiếm theo category_id cực kỳ nhanh:
    List<Product> findByCategoryId(Integer categoryId);
}
