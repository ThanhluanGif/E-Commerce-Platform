package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.ShopDTO;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserRole;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.ShopRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    public Shop registerShop(Integer userId, ShopDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        if (shopRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("Người dùng đã đăng ký gian hàng rồi!");
        }

        String slug = dto.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = dto.getName().toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
        }

        if (shopRepository.existsBySlug(slug)) {
            throw new BadRequestException("Slug gian hàng đã tồn tại! Vui lòng chọn tên khác.");
        }

        Shop shop = Shop.builder()
                .user(user)
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .logoUrl(dto.getLogoUrl())
                .bannerUrl(dto.getBannerUrl())
                .build();

        Shop savedShop = shopRepository.save(shop);

        // Update User Role to SELLER if it is currently CUSTOMER
        if (user.getRole() == UserRole.CUSTOMER) {
            user.setRole(UserRole.SELLER);
            userRepository.save(user);
        }

        return savedShop;
    }

    public Shop getShopByUserId(Integer userId) {
        return shopRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng chưa đăng ký gian hàng!"));
    }

    public Shop getShopBySlug(String slug) {
        return shopRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gian hàng!"));
    }

    public Shop getShopById(Integer id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gian hàng!"));
    }

    public Shop updateShop(Integer userId, ShopDTO dto) {
        Shop shop = getShopByUserId(userId);
        shop.setName(dto.getName());
        shop.setDescription(dto.getDescription());
        if (dto.getLogoUrl() != null) shop.setLogoUrl(dto.getLogoUrl());
        if (dto.getBannerUrl() != null) shop.setBannerUrl(dto.getBannerUrl());
        return shopRepository.save(shop);
    }

    public Shop approveShop(Integer id) {
        Shop shop = getShopById(id);
        shop.setVerified(true);
        return shopRepository.save(shop);
    }

    public Shop suspendShop(Integer id, boolean suspend) {
        Shop shop = getShopById(id);
        shop.setActive(!suspend);
        return shopRepository.save(shop);
    }

    public ShopDTO convertToDTO(Shop shop) {
        return ShopDTO.builder()
                .id(shop.getId())
                .userId(shop.getUser().getId())
                .username(shop.getUser().getUsername())
                .name(shop.getName())
                .slug(shop.getSlug())
                .description(shop.getDescription())
                .logoUrl(shop.getLogoUrl())
                .bannerUrl(shop.getBannerUrl())
                .rating(shop.getRating())
                .reviewCount(shop.getReviewCount())
                .verified(shop.isVerified())
                .active(shop.isActive())
                .joinDate(shop.getJoinDate())
                .followerCount(shop.getFollowerCount())
                .build();
    }
}
