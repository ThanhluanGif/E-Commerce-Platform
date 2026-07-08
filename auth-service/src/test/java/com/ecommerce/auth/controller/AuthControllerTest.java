package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.RedisTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTokenService redisTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USERNAME = "testsessionuser";
    private static final String PASSWORD = "password123";
    private static final String EMAIL = "testsession@example.com";
    private static final String PHONE = "0977777777";
    private static final String FULL_NAME = "Test Session User";

    @BeforeEach
    public void setup() {
        userRepository.findByUsername(USERNAME).ifPresent(user -> {
            userRepository.delete(user);
        });
    }

    @Test
    public void testFullAuthenticationLifecycle() throws Exception {
        // 1. Register User
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .email(EMAIL)
                .phone(PHONE)
                .fullName(FULL_NAME)
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie refreshCookie = regResult.getResponse().getCookie("refresh_token");
        assertNotNull(refreshCookie);
        assertNotNull(refreshCookie.getValue());
        assertTrue(refreshCookie.isHttpOnly());

        // Decode register response to get access token and userId
        String regResponseStr = regResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(regResponseStr);
        String accessToken = rootNode.path("data").path("accessToken").asText();
        Long userId = rootNode.path("data").path("user").path("id").asLong();

        // 2. Verify Refresh Token is in Redis
        String storedRefreshToken = redisTokenService.getRefreshToken(userId);
        assertNotNull(storedRefreshToken);
        assertEquals(refreshCookie.getValue(), storedRefreshToken);

        // 3. Call Refresh Endpoint (Wait 1 second to ensure JWT timestamp differs)
        Thread.sleep(1000);
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andReturn();

        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertNotNull(newRefreshCookie);
        assertNotEquals(refreshCookie.getValue(), newRefreshCookie.getValue());

        String refreshResponseStr = refreshResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode refreshNode = objectMapper.readTree(refreshResponseStr);
        String newAccessToken = refreshNode.path("data").path("accessToken").asText();
        assertNotNull(newAccessToken);
        assertNotEquals(accessToken, newAccessToken);

        // 4. Verify original access token is NOT blacklisted yet
        assertFalse(redisTokenService.isAccessTokenBlacklisted(newAccessToken));

        // 5. Logout using new Access Token
        MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        // Verify cookie is cleared
        Cookie clearedCookie = logoutResult.getResponse().getCookie("refresh_token");
        assertNotNull(clearedCookie);
        assertEquals(0, clearedCookie.getMaxAge());

        // Verify refresh token deleted from Redis
        assertNull(redisTokenService.getRefreshToken(userId));

        // Verify new access token is blacklisted in Redis
        assertTrue(redisTokenService.isAccessTokenBlacklisted(newAccessToken));
    }
}
