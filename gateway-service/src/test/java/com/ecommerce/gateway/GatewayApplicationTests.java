package com.ecommerce.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    private static final String SECRET = "9a4f2c8d3b7a1e5f8g0h2i4j6k8l0m2n4o6p8q0r2s4t6u8v0w2x4y6z8A0B2C4D6E8F0G2H4I6J8K0L2M4N6O8P0Q2R4S6T8U0V2W4X6Y8Z0a1b2c3d4e5f6g7h8";

    private String generateToken(Long id, String username, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .claim("id", id)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    @Test
    public void whenRequestingProtectedWithoutToken_thenUnauthorized() {
        webTestClient.get()
                .uri("/api/v1/products/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.UNAUTHORIZED.value())
                .jsonPath("$.error").isEqualTo("Unauthorized")
                .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    public void whenRequestingProtectedWithInvalidToken_thenUnauthorized() {
        webTestClient.get()
                .uri("/api/v1/products/1")
                .header("Authorization", "Bearer invalid-token-string")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.UNAUTHORIZED.value())
                .jsonPath("$.error").isEqualTo("Unauthorized")
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("JWT signature verification or expiration check failed"))
                .jsonPath("$.timestamp").exists();
    }

    @Test
    public void whenRequestingPublicPath_thenBypassedAndBadGatewayOrRouted() {
        webTestClient.post()
                .uri("/api/v1/auth/login")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_GATEWAY.value())
                .jsonPath("$.error").isEqualTo("Bad Gateway")
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Downstream service is currently unreachable"))
                .jsonPath("$.timestamp").exists();
    }

    @Test
    public void whenRequestingProtectedWithValidToken_thenBypassedAndBadGateway() {
        String token = generateToken(123L, "testuser", List.of("ROLE_USER"));

        webTestClient.get()
                .uri("/api/v1/products/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_GATEWAY.value())
                .jsonPath("$.error").isEqualTo("Bad Gateway")
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Downstream service is currently unreachable"));
    }
}
