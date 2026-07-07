package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategoryTree();
}
