package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.UserResponse;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.model.UserStatus;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtTokenProvider;
import com.ecommerce.auth.service.RedisTokenService;
import com.ecommerce.common.exception.AppException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTokenService redisTokenService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationInMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Phone number already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Default user role ROLE_USER not found in database"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .avatarUrl(request.getAvatarUrl())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);

        List<String> roles = savedUser.getRoles().stream()
                .map(Role::getName)
                .toList();

        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getUsername(), roles);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getUsername());

        // Save refresh token to Redis
        redisTokenService.saveRefreshToken(savedUser.getId(), refreshToken, refreshExpirationInMs);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(HttpStatus.FORBIDDEN, "Your account has been blocked");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
            String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

            // Save refresh token to Redis
            redisTokenService.saveRefreshToken(user.getId(), refreshToken, refreshExpirationInMs);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(mapToUserResponse(user))
                    .build();
        } catch (Exception ex) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Claims claims = tokenProvider.getClaimsFromToken(refreshToken);
        String username = claims.getSubject();

        User user = userRepository.findByUsername(username)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(HttpStatus.FORBIDDEN, "Your account has been blocked");
        }

        String storedToken = redisTokenService.getRefreshToken(user.getId());
        boolean isGrace = false;

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            String graceToken = redisTokenService.getGraceRefreshToken(user.getId());
            if (graceToken != null && graceToken.equals(refreshToken)) {
                isGrace = true;
            } else {
                throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or revoked");
            }
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String newRefreshToken;

        if (isGrace) {
            newRefreshToken = storedToken != null ? storedToken : tokenProvider.generateRefreshToken(user.getUsername());
            if (storedToken == null) {
                redisTokenService.saveRefreshToken(user.getId(), newRefreshToken, refreshExpirationInMs);
            }
        } else {
            newRefreshToken = tokenProvider.generateRefreshToken(user.getUsername());
            redisTokenService.saveRefreshTokenWithGracePeriod(user.getId(), newRefreshToken, refreshToken, refreshExpirationInMs);
        }

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Access token is empty");
        }

        Claims claims;
        try {
            claims = tokenProvider.getClaimsFromToken(accessToken);
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            claims = ex.getClaims();
        } catch (Exception ex) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }

        Long userId = null;
        Object idObj = claims.get("id");
        if (idObj instanceof Number) {
            userId = ((Number) idObj).longValue();
        }

        if (userId != null) {
            redisTokenService.deleteRefreshToken(userId);
        }

        java.util.Date expiration = claims.getExpiration();
        if (expiration != null) {
            long remainingTimeMs = expiration.getTime() - System.currentTimeMillis();
            if (remainingTimeMs > 0) {
                redisTokenService.blacklistAccessToken(accessToken, remainingTimeMs);
            }
        }
    }
}
