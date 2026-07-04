package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ProductDTO;
import com.ecommerce.ecommerceapi.dto.ProductImageDTO;
import com.ecommerce.ecommerceapi.dto.ProductVariantDTO;
import com.ecommerce.ecommerceapi.entity.Category;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.ProductVariant;
import com.ecommerce.ecommerceapi.service.CategoryService;
import com.ecommerce.ecommerceapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // 1. GET: Lấy toàn bộ sản phẩm có phân trang, tìm kiếm và lọc (http://localhost:8080/api/products)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer shopId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductDTO> products = productService.filterProducts(name, categoryId, shopId, minPrice, maxPrice, active, pageable)
                .map(this::convertToDTO);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công!", products));
    }

    // Lấy sản phẩm theo danh mục ID
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getByCategoryId(@PathVariable Integer categoryId) {
        List<ProductDTO> products = productService.getProductsByCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm theo danh mục thành công!", products));
    }

    // 2. GET: Lấy chi tiết 1 sản phẩm (http://localhost:8080/api/products/{id})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getById(@PathVariable Integer id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sản phẩm thành công!", convertToDTO(product)));
    }

    // 3. POST: Thêm mới sản phẩm
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> create(@Valid @RequestBody ProductDTO productDTO) {
        Category category = categoryService.getCategoryById(productDTO.getCategoryId());
        Product product = Product.builder()
                .name(productDTO.getName())
                .slug(productDTO.getSlug())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .salePrice(productDTO.getSalePrice())
                .stockQuantity(productDTO.getStockQuantity())
                .imageUrl(productDTO.getImageUrl())
                .active(productDTO.getActive() != null ? productDTO.getActive() : true)
                .category(category)
                .build();
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(ApiResponse.success("Tạo sản phẩm thành công!", convertToDTO(savedProduct)));
    }

    // 4. PUT: Cập nhật sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> update(@PathVariable Integer id, @Valid @RequestBody ProductDTO productDTO) {
        Product product = productService.getProductById(id);
        Category category = categoryService.getCategoryById(productDTO.getCategoryId());

        product.setName(productDTO.getName());
        product.setSlug(productDTO.getSlug());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setActive(productDTO.getActive() != null ? productDTO.getActive() : true);
        product.setCategory(category);

        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công!", convertToDTO(savedProduct)));
    }

    // 5. DELETE: Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công!"));
    }

    private ProductDTO convertToDTO(Product product) {
        List<ProductImageDTO> imageDTOs = null;
        if (product.getImages() != null) {
            imageDTOs = product.getImages().stream()
                    .map(img -> ProductImageDTO.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .sortOrder(img.getSortOrder())
                            .build())
                    .toList();
        }

        List<ProductVariantDTO> variantDTOs = null;
        if (product.getVariants() != null) {
            variantDTOs = product.getVariants().stream()
                    .map(v -> ProductVariantDTO.builder()
                            .id(v.getId())
                            .productId(product.getId())
                            .sku(v.getSku())
                            .name(v.getName())
                            .price(v.getPrice())
                            .salePrice(v.getSalePrice())
                            .stockQuantity(v.getStockQuantity())
                            .imageUrl(v.getImageUrl())
                            .build())
                    .toList();
        }

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getName() : null)
                .images(imageDTOs)
                .variants(variantDTOs)
                .build();
    }
}
