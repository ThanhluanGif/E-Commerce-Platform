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

    @Autowired
    private com.ecommerce.ecommerceapi.repository.ReferralRepository referralRepository;

    public UserDTO register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã được đăng ký!");
        }

        // Tạo mã referral code duy nhất cho người dùng mới
        String selfReferralCode;
        do {
            selfReferralCode = "REF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.findByReferralCode(selfReferralCode).isPresent());

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(UserRole.CUSTOMER)
                .address(request.getAddress())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .referralCode(selfReferralCode)
                .build();

        User savedUser = userRepository.save(user);

        // Xử lý mã giới thiệu nếu người đăng ký nhập vào
        if (request.getReferralCode() != null && !request.getReferralCode().trim().isEmpty()) {
            userRepository.findByReferralCode(request.getReferralCode().trim())
                .ifPresent(referrer -> {
                    com.ecommerce.ecommerceapi.entity.Referral referral = com.ecommerce.ecommerceapi.entity.Referral.builder()
                            .referrer(referrer)
                            .referee(savedUser)
                            .status("PENDING")
                            .rewarded(false)
                            .build();
                    referralRepository.save(referral);
                });
        }

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
                .referralCode(user.getReferralCode())
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
