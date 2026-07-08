package com.ecommerce.product.config;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductStatus;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.mapper.ProductDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexInitializer implements CommandLineRunner {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        try {
            log.info("Initializing Elasticsearch index for ProductDocument...");
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (!indexOps.exists()) {
                log.info("Index products_index does not exist. Creating index...");
                indexOps.create();
                indexOps.putMapping();
                log.info("Index products_index created successfully with mapping.");
            } else {
                log.info("Index products_index already exists.");
            }

            // Bootstrap active products from PostgreSQL to Elasticsearch index
            log.info("Bootstrapping active products to Elasticsearch index...");
            int page = 0;
            int size = 100;
            int count = 0;
            org.springframework.data.domain.Page<Product> productPage;

            do {
                productPage = productRepository.findByStatusAndDeletedAtIsNull(
                        ProductStatus.ACTIVE, org.springframework.data.domain.PageRequest.of(page, size));

                for (Product product : productPage.getContent()) {
                    try {
                        ProductDocument doc = ProductDocumentMapper.toDocument(product);
                        elasticsearchOperations.save(doc);
                        count++;
                    } catch (Exception e) {
                        log.error("Failed to bootstrap product ID {} to Elasticsearch: {}", product.getId(), e.getMessage());
                    }
                }

                page++;
            } while (productPage.hasNext());

            log.info("Successfully bootstrapped {} active products to Elasticsearch.", count);
        } catch (Exception e) {
            log.error("Error occurred during Elasticsearch index initialization: {}", e.getMessage());
        }
    }
}
