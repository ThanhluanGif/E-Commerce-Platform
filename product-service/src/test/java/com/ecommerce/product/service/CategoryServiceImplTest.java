package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getCategoryTree_shouldBuildHierarchyTreeCorrectly() {
        Category root1 = Category.builder()
                .id(1L)
                .parent(null)
                .name("Electronics")
                .slug("electronics")
                .level(0)
                .sortOrder(1)
                .isActive(true)
                .build();

        Category child1_1 = Category.builder()
                .id(2L)
                .parent(root1)
                .name("Smartphones")
                .slug("smartphones")
                .level(1)
                .sortOrder(1)
                .isActive(true)
                .build();

        Category child1_2 = Category.builder()
                .id(3L)
                .parent(root1)
                .name("Laptops")
                .slug("laptops")
                .level(1)
                .sortOrder(2)
                .isActive(true)
                .build();

        Category grandchild1_1_1 = Category.builder()
                .id(4L)
                .parent(child1_1)
                .name("Android Phones")
                .slug("android-phones")
                .level(2)
                .sortOrder(1)
                .isActive(true)
                .build();

        Category root2 = Category.builder()
                .id(5L)
                .parent(null)
                .name("Fashion")
                .slug("fashion")
                .level(0)
                .sortOrder(2)
                .isActive(true)
                .build();

        List<Category> allCategories = List.of(root1, child1_1, child1_2, grandchild1_1_1, root2);
        when(categoryRepository.findAllByIsActiveTrueOrderBySortOrderAsc()).thenReturn(allCategories);

        List<CategoryResponse> tree = categoryService.getCategoryTree();

        assertThat(tree).hasSize(2);

        CategoryResponse electronics = tree.get(0);
        assertThat(electronics.getId()).isEqualTo(1L);
        assertThat(electronics.getName()).isEqualTo("Electronics");
        assertThat(electronics.getChildren()).hasSize(2);

        CategoryResponse smartphones = electronics.getChildren().stream()
                .filter(c -> c.getId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertThat(smartphones.getName()).isEqualTo("Smartphones");
        assertThat(smartphones.getChildren()).hasSize(1);

        CategoryResponse android = smartphones.getChildren().get(0);
        assertThat(android.getId()).isEqualTo(4L);
        assertThat(android.getName()).isEqualTo("Android Phones");
        assertThat(android.getChildren()).isEmpty();

        CategoryResponse laptops = electronics.getChildren().stream()
                .filter(c -> c.getId().equals(3L))
                .findFirst()
                .orElseThrow();
        assertThat(laptops.getName()).isEqualTo("Laptops");
        assertThat(laptops.getChildren()).isEmpty();

        CategoryResponse fashion = tree.get(1);
        assertThat(fashion.getId()).isEqualTo(5L);
        assertThat(fashion.getName()).isEqualTo("Fashion");
        assertThat(fashion.getChildren()).isEmpty();
    }
}
