package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.CategoryDTO;
import com.ecommerce.ecommerceapi.entity.Category;
import com.ecommerce.ecommerceapi.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. GET: Lấy toàn bộ danh mục (http://localhost:8080/api/categories)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAll() {
        List<CategoryDTO> categories = categoryService.getAllCategories().stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công!", categories));
    }

    // Lấy cấu trúc cây danh mục (http://localhost:8080/api/categories/tree)
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getTree() {
        List<CategoryDTO> tree = categoryService.getCategoryTree().stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu trúc cây danh mục thành công!", tree));
    }

    // 2. GET: Lấy chi tiết 1 danh mục theo ID (http://localhost:8080/api/categories/{id})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getById(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết danh mục thành công!", convertToDTO(category)));
    }

    // 3. POST: Thêm mới danh mục
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> create(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category parent = null;
        if (categoryDTO.getParentId() != null) {
            parent = categoryService.getCategoryById(categoryDTO.getParentId());
        }
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .slug(categoryDTO.getSlug())
                .description(categoryDTO.getDescription())
                .imageUrl(categoryDTO.getImageUrl())
                .parent(parent)
                .build();
        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Tạo danh mục thành công!", convertToDTO(savedCategory)));
    }

    // 4. PUT: Cập nhật danh mục theo ID
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> update(@PathVariable Integer id, @Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = categoryService.getCategoryById(id);
        Category parent = null;
        if (categoryDTO.getParentId() != null) {
            parent = categoryService.getCategoryById(categoryDTO.getParentId());
        }
        category.setName(categoryDTO.getName());
        category.setSlug(categoryDTO.getSlug());
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());
        category.setParent(parent);

        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công!", convertToDTO(savedCategory)));
    }

    // 5. DELETE: Xóa danh mục theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công!"));
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .build();
        if (category.getChildren() != null) {
            dto.setChildren(category.getChildren().stream().map(this::convertToDTO).toList());
        }
        return dto;
    }
}
