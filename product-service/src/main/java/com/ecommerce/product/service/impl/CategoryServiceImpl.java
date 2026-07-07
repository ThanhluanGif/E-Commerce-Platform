package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getCategoryTree() {
        List<Category> allActiveCategories = categoryRepository.findAllByIsActiveTrueOrderBySortOrderAsc();

        // Group categories by parent ID.
        // If parent is null, group under ID 0L (representing root nodes)
        Map<Long, List<Category>> childrenMap = allActiveCategories.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParent() != null ? c.getParent().getId() : 0L
                ));

        // Get roots from the 0L key
        List<Category> roots = childrenMap.getOrDefault(0L, List.of());

        // Recursively build the tree from roots
        return roots.stream()
                .map(root -> convertToResponseAndBuildTree(root, childrenMap))
                .collect(Collectors.toList());
    }

    private CategoryResponse convertToResponseAndBuildTree(Category category, Map<Long, List<Category>> childrenMap) {
        CategoryResponse response = convertToResponse(category);

        List<Category> children = childrenMap.getOrDefault(category.getId(), List.of());
        if (!children.isEmpty()) {
            List<CategoryResponse> childResponses = children.stream()
                    .map(child -> convertToResponseAndBuildTree(child, childrenMap))
                    .collect(Collectors.toList());
            response.setChildren(childResponses);
        } else {
            response.setChildren(new ArrayList<>());
        }

        return response;
    }

    private CategoryResponse convertToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .build();
    }
}
