package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public List<Product> getSimilarProducts(Integer productId, int limit) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return new ArrayList<>();

        BigDecimal price = product.getPrice();
        BigDecimal range = price.multiply(BigDecimal.valueOf(0.3)); // +/- 30% price range
        BigDecimal minPrice = price.subtract(range);
        BigDecimal maxPrice = price.add(range);

        return productRepository.findSimilarProducts(
                product.getCategory().getId(),
                productId,
                minPrice,
                maxPrice,
                PageRequest.of(0, limit)
        );
    }

    public List<Product> getCollaborativeFilteringRecommendations(Integer productId, int limit) {
        return orderItemRepository.findFrequentlyBoughtTogether(productId, PageRequest.of(0, limit));
    }

    public List<Product> getTrendingProducts(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> topProductData = userActivityRepository.findTopProductsByActivityTypeSince(
                ActivityType.VIEW, 
                since, 
                PageRequest.of(0, limit)
        );

        List<Integer> ids = topProductData.stream()
                .map(data -> (Integer) data[0])
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            // Fallback to top products order by created time if no activity logs
            return productRepository.findAll(PageRequest.of(0, limit)).getContent();
        }

        return productRepository.findAllById(ids);
    }

    public List<Product> getRecentlyViewed(Integer userId, int limit) {
        if (userId == null) return new ArrayList<>();

        List<UserActivity> views = userActivityRepository.findByUserIdAndActivityTypeOrderByTimestampDesc(
                userId, 
                ActivityType.VIEW, 
                PageRequest.of(0, limit * 2) // Fetch a bit extra to filter duplicates
        );

        return views.stream()
                .map(UserActivity::getProduct)
                .filter(p -> p != null && p.isActive())
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
