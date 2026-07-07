package com.ecommerce.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRefreshToken(Long userId, String token, long durationMs) {
        String key = "user:" + userId + ":refresh_token";
        redisTemplate.opsForValue().set(key, token, durationMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long userId) {
        String key = "user:" + userId + ":refresh_token";
        Object val = redisTemplate.opsForValue().get(key);
        return val != null ? val.toString() : null;
    }

    public void deleteRefreshToken(Long userId) {
        String key = "user:" + userId + ":refresh_token";
        redisTemplate.delete(key);
    }

    public void blacklistAccessToken(String token, long remainingTimeMs) {
        String key = "blacklist:" + token;
        if (remainingTimeMs > 0) {
            redisTemplate.opsForValue().set(key, "true", remainingTimeMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
