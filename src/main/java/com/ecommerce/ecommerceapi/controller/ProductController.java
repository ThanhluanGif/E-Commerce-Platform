package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. GET: Lấy toàn bộ sản phẩm (http://localhost:8080/api/products)
    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    // 2. GET: Lấy chi tiết 1 sản phẩm (http://localhost:8080/api/products/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // 3. POST: Thêm mới sản phẩm
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    // 4. PUT: Cập nhật sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Integer id, @RequestBody Product productDetails) {
        Product product = productService.getProductById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory()); // Gắn danh mục cho sản phẩm

        return ResponseEntity.ok(productService.saveProduct(product));
    }

    // 5. DELETE: Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công!");
    }
}
