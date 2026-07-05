package com.ecommerce.ecommerceapi.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        final int limit = (uri.contains("/api/auth/login") || uri.contains("/api/auth/register")) ? 10 : 100;

        String key = ip + ":" + (uri.contains("/api/auth/") ? "auth" : "general");
        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket(limit));

        if (!bucket.allowRequest()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Yêu cầu quá nhanh. Vui lòng thử lại sau 1 phút.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class RateLimitBucket {
        private final int limit;
        private final AtomicInteger count = new AtomicInteger(0);
        private long resetTime;

        public RateLimitBucket(int limit) {
            this.limit = limit;
            this.resetTime = System.currentTimeMillis() + 60000;
        }

        public synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            if (now > resetTime) {
                count.set(0);
                resetTime = now + 60000;
            }
            return count.incrementAndGet() <= limit;
        }
    }
}
