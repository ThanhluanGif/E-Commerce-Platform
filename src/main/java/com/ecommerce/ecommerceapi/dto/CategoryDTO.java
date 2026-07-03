package com.ecommerce.ecommerceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer parentId;
    private String parentName;
    private List<CategoryDTO> children;
}
