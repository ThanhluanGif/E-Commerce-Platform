package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Category;
import com.ecommerce.ecommerceapi.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy tất cả danh mục
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    // Lấy danh mục theo ID
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
    }

    // Thêm mới hoặc Cập nhật danh mục
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Xóa danh mục
    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id); // Kiểm tra xem có tồn tại không trước khi xóa
        categoryRepository.delete(category);
    }
}
