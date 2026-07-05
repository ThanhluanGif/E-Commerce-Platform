package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.RegisterRequest;
import com.ecommerce.ecommerceapi.dto.UserDTO;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserRole;
import com.ecommerce.ecommerceapi.exception.DuplicateResourceException;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("testuser@example.com");
        registerRequest.setAddress("Hanoi, Vietnam");
        registerRequest.setPhone("0987654321");
    }

    @Test
    public void testRegister_Success() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        
        User mockSavedUser = User.builder()
                .id(1)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash("encoded_password")
                .role(UserRole.CUSTOMER)
                .build();
                
        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

        UserDTO result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("testuser@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegister_DuplicateUsername_ThrowsException() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
