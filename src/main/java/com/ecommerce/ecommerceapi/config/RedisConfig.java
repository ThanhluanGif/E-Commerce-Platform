package com.ecommerce.ecommerceapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    @SuppressWarnings("deprecation")
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình serializer mặc định cho Redis Cache
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // TTL mặc định 1 giờ
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Cấu hình TTL riêng cho từng cache name
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Categories tree cache - 2 giờ
        cacheConfigurations.put("categories", config.entryTtl(Duration.ofHours(2)));
        
        // Single Product details cache - 30 phút
        cacheConfigurations.put("products", config.entryTtl(Duration.ofMinutes(30)));
        
        // Flash sale active cache - 5 phút
        cacheConfigurations.put("flashsales", config.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
