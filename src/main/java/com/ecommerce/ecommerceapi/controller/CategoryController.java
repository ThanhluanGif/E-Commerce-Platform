package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.entity.Category;
import com.ecommerce.ecommerceapi.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*") // Cho phép tất cả các nguồn truy cập
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. GET: Lấy toàn bộ danh mục (http://localhost:8080/api/categories)
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAllCategories();
    }
    // 2. GET: Lấy chi tiết 1 danh mục theo ID (http://localhost:8080/api/categories/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // 3. POST: Thêm mới danh mục
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    //4. PUT: Cập nhật danh mục theo ID
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id, @RequestBody Category categoryDetails) {
        Category category = categoryService.getCategoryById(id);
        category.setName(categoryDetails.getName());
        // Trường parent_id xử lý đệ quy (nếu có truyền)
        category.setParent(categoryDetails.getParent());

        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    //5. DELETE: Xóa danh mục theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

}
