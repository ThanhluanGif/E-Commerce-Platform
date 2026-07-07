package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getCategoryTree_shouldReturnCategoryTree() throws Exception {
        CategoryResponse root = CategoryResponse.builder()
                .id(1L)
                .name("Root Category")
                .slug("root-category")
                .level(0)
                .sortOrder(1)
                .children(List.of(
                        CategoryResponse.builder()
                                .id(2L)
                                .parentId(1L)
                                .name("Child Category")
                                .slug("child-category")
                                .level(1)
                                .sortOrder(1)
                                .children(List.of())
                                .build()
                ))
                .build();

        when(categoryService.getCategoryTree()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/v1/categories/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category tree retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Root Category"))
                .andExpect(jsonPath("$.data[0].children[0].id").value(2))
                .andExpect(jsonPath("$.data[0].children[0].name").value("Child Category"));
    }
}
