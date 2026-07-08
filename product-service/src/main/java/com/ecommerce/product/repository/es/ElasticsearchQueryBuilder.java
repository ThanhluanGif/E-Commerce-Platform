package com.ecommerce.product.repository.es;

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import lombok.experimental.UtilityClass;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ElasticsearchQueryBuilder {

    public static NativeQuery buildSearchQuery(String keyword, List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 1. Only search active products
        filterQueries.add(Query.of(q -> q.term(t -> t.field("status").value("ACTIVE"))));

        // 2. Fuzzy search on name and description
        if (keyword != null && !keyword.trim().isEmpty()) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .fields("name^2", "description")
                    .query(keyword.trim())
                    .fuzziness("AUTO")
            )));
        }

        // 3. Filter by categories (terms query matching any descendant ID)
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<co.elastic.clients.elasticsearch._types.FieldValue> fieldValues = categoryIds.stream()
                    .map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id))
                    .toList();
            filterQueries.add(Query.of(q -> q.terms(t -> t
                    .field("categoryId")
                    .terms(ts -> ts.value(fieldValues))
            )));
        }

        // 4. Exact filter by price range in nested variants
        if (minPrice != null || maxPrice != null) {
            filterQueries.add(Query.of(q -> q.nested(n -> n
                    .path("variants")
                    .scoreMode(ChildScoreMode.None)
                    .query(nq -> nq.range(r -> {
                        r.field("variants.price");
                        if (minPrice != null) {
                            r.gte(JsonData.of(minPrice.doubleValue()));
                        }
                        if (maxPrice != null) {
                            r.lte(JsonData.of(maxPrice.doubleValue()));
                        }
                        return r;
                    }))
            )));
        }

        // Build the boolean query
        Query boolQuery = Query.of(q -> q.bool(b -> b
                .must(mustQueries)
                .filter(filterQueries)
        ));

        return new NativeQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();
    }
}
