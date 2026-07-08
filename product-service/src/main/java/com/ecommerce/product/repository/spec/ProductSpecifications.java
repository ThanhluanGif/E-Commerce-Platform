package com.ecommerce.product.repository.spec;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductStatus;
import com.ecommerce.product.entity.ProductVariant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> searchProducts(String keyword, List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Not deleted and active status
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            // 2. Keyword fuzzy/unaccent/like on name and description
            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedKeyword = "%" + keyword.trim().toLowerCase() + "%";
                Expression<String> unaccentName = cb.function("unaccent", String.class, cb.lower(root.get("name")));
                Expression<String> unaccentDesc = cb.function("unaccent", String.class, cb.lower(root.get("description")));
                Expression<String> unaccentSearch = cb.function("unaccent", String.class, cb.literal(normalizedKeyword));

                predicates.add(cb.or(
                        cb.like(unaccentName, unaccentSearch),
                        cb.like(unaccentDesc, unaccentSearch)
                ));
            }

            // 3. Category ID filter (IN clause for descendants)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // 4. Price range filter on variants (using distinct subquery to avoid product duplicate rows)
            if (minPrice != null || maxPrice != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<ProductVariant> variantRoot = subquery.from(ProductVariant.class);
                subquery.select(variantRoot.get("product").get("id"));

                List<Predicate> subPredicates = new ArrayList<>();
                subPredicates.add(cb.equal(variantRoot.get("product"), root));
                subPredicates.add(cb.isNull(variantRoot.get("deletedAt")));

                if (minPrice != null) {
                    subPredicates.add(cb.greaterThanOrEqualTo(variantRoot.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    subPredicates.add(cb.lessThanOrEqualTo(variantRoot.get("price"), maxPrice));
                }
                subquery.where(subPredicates.toArray(new Predicate[0]));

                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
