package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ProductDTO;
import com.ecommerce.ecommerceapi.dto.ProductImageDTO;
import com.ecommerce.ecommerceapi.dto.ProductVariantDTO;
import com.ecommerce.ecommerceapi.entity.Category;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.entity.ProductVariant;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.ProductVariantRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.CategoryService;
import com.ecommerce.ecommerceapi.service.ProductService;
import com.ecommerce.ecommerceapi.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    private Shop getSellerShop(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            throw new BadRequestException("Chưa đăng nhập!");
        }
        return shopService.getShopByUserId(userId);
    }

    // 1. GET: Lấy toàn bộ sản phẩm của shop tôi
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getMyProducts(
            Principal principal,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Shop shop = getSellerShop(principal);
        Page<ProductDTO> products = productRepository.filterByShop(shop.getId(), name, pageable)
                .map(this::convertToDTO);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công!", products));
    }

    // 2. POST: Đăng sản phẩm mới cho shop tôi
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(Principal principal, @Valid @RequestBody ProductDTO productDTO) {
        Shop shop = getSellerShop(principal);
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
                .shop(shop) // Gán shop của seller
                .build();
                
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(ApiResponse.success("Đăng sản phẩm thành công!", convertToDTO(savedProduct)));
    }

    // 3. PUT: Cập nhật sản phẩm của shop tôi
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(Principal principal, @PathVariable Integer id, @Valid @RequestBody ProductDTO productDTO) {
        Shop shop = getSellerShop(principal);
        Product product = productService.getProductById(id);
        
        if (product.getShop() == null || !product.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa sản phẩm này!");
        }
        
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

    // 4. DELETE: Xóa sản phẩm của shop tôi
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(Principal principal, @PathVariable Integer id) {
        Shop shop = getSellerShop(principal);
        Product product = productService.getProductById(id);
        
        if (product.getShop() == null || !product.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Bạn không có quyền xóa sản phẩm này!");
        }
        
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công!"));
    }

    // 5. POST: Thêm biến thể cho sản phẩm
    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> createVariant(
            Principal principal,
            @PathVariable Integer productId,
            @Valid @RequestBody ProductVariantDTO variantDTO
    ) {
        Shop shop = getSellerShop(principal);
        Product product = productService.getProductById(productId);
        
        if (product.getShop() == null || !product.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Bạn không có quyền quản lý sản phẩm này!");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(variantDTO.getSku())
                .name(variantDTO.getName())
                .price(variantDTO.getPrice())
                .salePrice(variantDTO.getSalePrice())
                .stockQuantity(variantDTO.getStockQuantity())
                .imageUrl(variantDTO.getImageUrl())
                .build();

        ProductVariant saved = productVariantRepository.save(variant);
        return ResponseEntity.ok(ApiResponse.success("Thêm biến thể thành công!", convertVariantToDTO(saved)));
    }

    // 6. PUT: Cập nhật biến thể sản phẩm
    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> updateVariant(
            Principal principal,
            @PathVariable Integer productId,
            @PathVariable Integer variantId,
            @Valid @RequestBody ProductVariantDTO variantDTO
    ) {
        Shop shop = getSellerShop(principal);
        Product product = productService.getProductById(productId);
        
        if (product.getShop() == null || !product.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Bạn không có quyền quản lý sản phẩm này!");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm!"));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Biến thể này không thuộc về sản phẩm đã chọn!");
        }

        variant.setSku(variantDTO.getSku());
        variant.setName(variantDTO.getName());
        variant.setPrice(variantDTO.getPrice());
        variant.setSalePrice(variantDTO.getSalePrice());
        variant.setStockQuantity(variantDTO.getStockQuantity());
        variant.setImageUrl(variantDTO.getImageUrl());

        ProductVariant saved = productVariantRepository.save(variant);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật biến thể thành công!", convertVariantToDTO(saved)));
    }

    // 7. DELETE: Xóa biến thể sản phẩm
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            Principal principal,
            @PathVariable Integer productId,
            @PathVariable Integer variantId
    ) {
        Shop shop = getSellerShop(principal);
        Product product = productService.getProductById(productId);
        
        if (product.getShop() == null || !product.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Bạn không có quyền quản lý sản phẩm này!");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm!"));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Biến thể này không thuộc về sản phẩm đã chọn!");
        }

        productVariantRepository.delete(variant);
        return ResponseEntity.ok(ApiResponse.success("Xóa biến thể thành công!"));
    }

    private ProductVariantDTO convertVariantToDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .sku(variant.getSku())
                .name(variant.getName())
                .price(variant.getPrice())
                .salePrice(variant.getSalePrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrl(variant.getImageUrl())
                .build();
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
