package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.auth.RegisterRequest;
import com.personalfinance.manager.dto.auth.RegisterResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ConflictException;
import com.personalfinance.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest request;
    private User mockUser;

    @BeforeEach
    void setUp() {
        request = RegisterRequest.builder()
                .username("user@example.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();

        mockUser = User.builder()
                .id(1L)
                .username("user@example.com")
                .password("encodedPassword123")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();
    }

    @Test
    void registerUser_success() {
        when(userRepository.existsByUsernameIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        RegisterResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        verify(userRepository, times(1)).existsByUsernameIgnoreCase("user@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_usernameConflict_throwsConflictException() {
        when(userRepository.existsByUsernameIgnoreCase("user@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerUser(request));

        verify(userRepository, times(1)).existsByUsernameIgnoreCase("user@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
