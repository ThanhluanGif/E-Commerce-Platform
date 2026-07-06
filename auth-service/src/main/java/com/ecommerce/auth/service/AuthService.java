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
import com.ecommerce.common.exception.AppException;
import lombok.RequiredArgsConstructor;
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
}
