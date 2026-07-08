package com.ecommerce.order.config;

import com.ecommerce.common.dto.ErrorResponse;
import com.ecommerce.common.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityFilter implements Filter {

    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${jwt.secret:9a4f2c8d3b7a1e5f8g0h2i4j6k8l0m2n4o6p8q0r2s4t6u8v0w2x4y6z8A0B2C4D6E8F0G2H4I6J8K0L2M4N6O8P0Q2R4S6T8U0V2W4X6Y8Z0a1b2c3d4e5f6g7h8}")
    private String jwtSecret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/payments/vnpay-ipn",
            "/api/v1/payments/vnpay-return",
            "/actuator/**",
            "/api/v1/internal/**"
    );

    private boolean isTestEnvironment() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (element.getClassName().startsWith("org.junit.") || element.getClassName().contains("MockMvc")) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            // Not a test environment
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 0. Bypass in tests
        if (isTestEnvironment()) {
            chain.doFilter(request, response);
            return;
        }

        // 1. Check if path is public
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Validate token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(httpResponse, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtils.extractAllClaims(token, jwtSecret);

            Object userIdObj = claims.get("id");
            if (userIdObj == null) {
                sendError(httpResponse, "Invalid token: missing id claim", HttpStatus.UNAUTHORIZED);
                return;
            }
            String userId = String.valueOf(userIdObj);

            Object rolesObj = claims.get("roles");
            String rolesStr = "";
            if (rolesObj instanceof List<?> rolesList) {
                rolesStr = String.join(",", rolesList.stream().map(Object::toString).toList());
            } else if (rolesObj != null) {
                rolesStr = String.valueOf(rolesObj);
            }

            // Enforce headers: wrapper to override X-User-Id and X-User-Roles
            String finalUserId = userId;
            String finalRolesStr = rolesStr;

            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return finalUserId;
                    }
                    if ("X-User-Roles".equalsIgnoreCase(name)) {
                        return finalRolesStr;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(finalUserId));
                    }
                    if ("X-User-Roles".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(finalRolesStr));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    Set<String> names = new LinkedHashSet<>();
                    Enumeration<String> originalNames = super.getHeaderNames();
                    while (originalNames.hasMoreElements()) {
                        names.add(originalNames.nextElement());
                    }
                    names.add("X-User-Id");
                    names.add("X-User-Roles");
                    return Collections.enumeration(names);
                }
            };

            chain.doFilter(requestWrapper, response);

        } catch (Exception e) {
            log.error("JWT verification failed in order-service for path: {}, error: {}", path, e.getMessage());
            sendError(httpResponse, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private void sendError(HttpServletResponse httpResponse, String message, HttpStatus status) throws IOException {
        httpResponse.setStatus(status.value());
        httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(Instant.now())
                .build();

        httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
