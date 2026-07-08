package com.ecommerce.common.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "feign.RequestInterceptor")
public class FeignLoggingConfiguration {

    @Bean
    public FeignCorrelationIdInterceptor feignCorrelationIdInterceptor() {
        return new FeignCorrelationIdInterceptor();
    }
}
