package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.auth.RegisterRequest;
import com.personalfinance.manager.dto.auth.RegisterResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ConflictException;
import com.personalfinance.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new ConflictException("Username is already taken");
        }

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .phoneNumber(request.getPhoneNumber())
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return RegisterResponse.builder()
            .message("User registered successfully")
            .userId(savedUser.getId())
            .build();
    }
}
