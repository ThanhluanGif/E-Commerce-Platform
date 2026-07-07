package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    // Lấy tất cả sản phẩm
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Lấy danh sách sản phẩm theo category ID
    public List<Product> getProductsByCategoryId(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Lấy sản phẩm theo ID
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + id));
    }

    // Thêm mới hoặc Cập nhật sản phẩm
    @CacheEvict(value = "products", key = "#product.id", condition = "#product.id != null")
    public Product saveProduct(Product product) {
        if (product.getDescription() != null) {
            product.setDescription(com.ecommerce.ecommerceapi.security.XssSanitizer.sanitize(product.getDescription()));
        }
        return productRepository.save(product);
    }

    // Xóa sản phẩm
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    // Lọc, tìm kiếm và phân trang sản phẩm
    public Page<Product> filterProducts(String name, Integer categoryId, Integer shopId, BigDecimal minPrice, BigDecimal maxPrice, Boolean active, Pageable pageable) {
        return productRepository.filterProducts(name, categoryId, shopId, minPrice, maxPrice, active, pageable);
    }
}
