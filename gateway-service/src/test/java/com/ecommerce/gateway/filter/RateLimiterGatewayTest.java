package com.ecommerce.gateway.filter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RateLimiterGatewayTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    public void testRateLimiting_TooManyRequests() {
        AtomicInteger requestCounter = new AtomicInteger(0);

        Mockito.when(redisRateLimiter.isAllowed(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    int count = requestCounter.incrementAndGet();
                    boolean allowed = count <= 20;
                    Map<String, String> headers = new HashMap<>();
                    headers.put(RedisRateLimiter.REMAINING_HEADER, String.valueOf(Math.max(0, 20 - count)));
                    headers.put(RedisRateLimiter.REPLENISH_RATE_HEADER, "10");
                    headers.put(RedisRateLimiter.BURST_CAPACITY_HEADER, "20");
                    return Mono.just(new RateLimiter.Response(allowed, headers));
                });

        int totalRequests = 30;
        int expectedSuccess = 20;
        int expected429 = 10;

        int successCount = 0;
        int tooManyRequestsCount = 0;

        for (int i = 0; i < totalRequests; i++) {
            org.springframework.http.HttpStatusCode status = webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();

            if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                tooManyRequestsCount++;
            } else if (status.value() == HttpStatus.BAD_GATEWAY.value()) {
                successCount++;
            }
        }

        System.out.println("Success count (routed): " + successCount);
        System.out.println("429 count (rate limited): " + tooManyRequestsCount);

        assertEquals(expectedSuccess, successCount);
        assertEquals(expected429, tooManyRequestsCount);
    }
}
