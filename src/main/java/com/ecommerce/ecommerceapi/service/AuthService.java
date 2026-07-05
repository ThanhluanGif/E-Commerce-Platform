package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.LoginRequest;
import com.ecommerce.ecommerceapi.dto.LoginResponse;
import com.ecommerce.ecommerceapi.dto.RegisterRequest;
import com.ecommerce.ecommerceapi.dto.UserDTO;
import com.ecommerce.ecommerceapi.dto.TokenRefreshRequest;
import com.ecommerce.ecommerceapi.dto.TokenRefreshResponse;
import com.ecommerce.ecommerceapi.entity.RefreshToken;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserRole;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.DuplicateResourceException;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    public UserDTO register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã được đăng ký!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(UserRole.CUSTOMER)
                .address(request.getAddress())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .build();

        User savedUser = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(savedUser);
            notificationService.createNotification(
                    savedUser.getId(),
                    "Chào mừng bạn đến với E-Shop!",
                    "Chào mừng bạn gia nhập E-Shop. Hãy bắt đầu mua sắm ngay hôm nay!",
                    null,
                    "/"
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email chào mừng: " + e.getMessage());
        }

        return convertToDTO(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Tài khoản hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        String token = jwtProvider.generateToken(user.getUsername());
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        String ipAddress = httpServletRequest.getRemoteAddr();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), deviceInfo, ipAddress);

        return new LoginResponse(token, refreshToken.getToken(), convertToDTO(user));
    }

    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .address(user.getAddress())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtProvider.generateToken(user.getUsername());
                    
                    // Token Rotation
                    String deviceInfo = httpServletRequest.getHeader("User-Agent");
                    String ipAddress = httpServletRequest.getRemoteAddr();
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId(), deviceInfo, ipAddress);
                    
                    return new TokenRefreshResponse(accessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new BadRequestException("Refresh token không hợp lệ hoặc đã hết hạn!"));
    }
}
