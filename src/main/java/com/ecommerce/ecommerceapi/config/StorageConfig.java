package com.ecommerce.ecommerceapi.config;

import com.ecommerce.ecommerceapi.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${app.storage.type:local}")
    private String storageType;

    @Autowired
    private ApplicationContext context;

    @Bean
    public StorageService storageService() {
        if ("s3".equalsIgnoreCase(storageType)) {
            return context.getBean("s3Storage", StorageService.class);
        }
        return context.getBean("localStorage", StorageService.class);
    }
}
