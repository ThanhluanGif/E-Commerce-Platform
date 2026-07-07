package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.config.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationFilterTest {

    private static final String SECRET = "9a4f2c8d3b7a1e5f8g0h2i4j6k8l0m2n4o6p8q0r2s4t6u8v0w2x4y6z8A0B2C4D6E8F0G2H4I6J8K0L2M4N6O8P0Q2R4S6T8U0V2W4X6Y8Z0a1b2c3d4e5f6g7h8";
    private AuthenticationFilter filter;
    private JwtProperties jwtProperties;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setPublicPaths(List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/actuator/**"
        ));

        filter = new AuthenticationFilter(jwtProperties, objectMapper);
    }

    private String generateToken(Object id, Object roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("testuser")
                .claim("id", id)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    @Test
    public void testFilter_PublicPath_Bypass() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] chainCalled = {false};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            assertNull(ex.getRequest().getHeaders().getFirst("X-User-Id"));
            assertNull(ex.getRequest().getHeaders().getFirst("X-User-Roles"));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainCalled[0]);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_ProtectedPath_MissingToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] chainCalled = {false};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertFalse(chainCalled[0]);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    public void testFilter_ProtectedPath_InvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] chainCalled = {false};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertFalse(chainCalled[0]);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_ProtectedPath_ValidToken_ListRoles() {
        String token = generateToken(123L, List.of("ROLE_USER", "ROLE_ADMIN"));
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] chainCalled = {false};
        final ServerWebExchange[] capturedExchange = {null};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            capturedExchange[0] = ex;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainCalled[0]);
        assertNotNull(capturedExchange[0]);
        assertEquals("123", capturedExchange[0].getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("ROLE_USER,ROLE_ADMIN", capturedExchange[0].getRequest().getHeaders().getFirst("X-User-Roles"));
    }

    @Test
    public void testFilter_ProtectedPath_ValidToken_SingleRole() {
        String token = generateToken(999L, "ROLE_USER");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] chainCalled = {false};
        final ServerWebExchange[] capturedExchange = {null};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            capturedExchange[0] = ex;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainCalled[0]);
        assertNotNull(capturedExchange[0]);
        assertEquals("999", capturedExchange[0].getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("ROLE_USER", capturedExchange[0].getRequest().getHeaders().getFirst("X-User-Roles"));
    }
}
