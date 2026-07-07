package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Category;
import com.ecommerce.ecommerceapi.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy tất cả danh mục
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    // Lấy cây thư mục (danh mục gốc và các con của nó)
    @Cacheable(value = "categories", key = "'tree'")
    public List<Category> getCategoryTree() {
        return categoryRepository.findByParentIsNull();
    }

    // Lấy danh mục theo ID
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy danh mục với id: " + id));
    }

    // Thêm mới hoặc Cập nhật danh mục
    @CacheEvict(value = "categories", allEntries = true)
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Xóa danh mục
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id); // Kiểm tra xem có tồn tại không trước khi xóa
        categoryRepository.delete(category);
    }
}
