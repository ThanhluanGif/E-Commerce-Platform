package com.ecommerce.gateway.filter;

import com.ecommerce.common.dto.ErrorResponse;
import com.ecommerce.gateway.config.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Clean headers to prevent client spoofing
        ServerHttpRequest cleanedRequest = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.keySet().removeIf(key -> key.toLowerCase().startsWith("x-user-"));
                })
                .build();
        ServerWebExchange cleanedExchange = exchange.mutate().request(cleanedRequest).build();
        
        String path = cleanedRequest.getURI().getPath();

        // 1. Check if path is public
        if (isPublicPath(path)) {
            return chain.filter(cleanedExchange);
        }

        // 2. Read Authorization header
        String authHeader = cleanedRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(cleanedExchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // 3. Parse and validate token
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract claims
            Object userIdObj = claims.get("id");
            if (userIdObj == null) {
                log.warn("JWT payload does not contain id claim for path: {}", path);
                return onError(cleanedExchange, "Invalid token payload: missing id claim", HttpStatus.UNAUTHORIZED);
            }
            String userId = String.valueOf(userIdObj);

            // Extract roles claim (can be a List of strings or a single String, or null)
            Object rolesObj = claims.get("roles");
            String rolesStr = "";
            if (rolesObj instanceof List<?> rolesList) {
                rolesStr = String.join(",", rolesList.stream().map(Object::toString).toList());
            } else if (rolesObj != null) {
                rolesStr = String.valueOf(rolesObj);
            }

            // 4. Inject X-User-Id and X-User-Roles into request headers
            ServerHttpRequest mutatedRequest = cleanedRequest.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Roles", rolesStr)
                    .build();

            return chain.filter(cleanedExchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT verification failed for path: {}, error: {}", path, e.getMessage());
            return onError(cleanedExchange, "JWT signature verification or expiration check failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        List<String> publicPaths = jwtProperties.getPublicPaths();
        if (publicPaths == null) {
            return false;
        }
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(err)
                .timestamp(Instant.now())
                .build();

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            log.error("Failed to serialize auth error response", e);
            bytes = String.format(
                    "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                    status.value(),
                    status.getReasonPhrase(),
                    err,
                    Instant.now().toString()
            ).getBytes(StandardCharsets.UTF_8);
        }

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1; // Run early
    }
}
