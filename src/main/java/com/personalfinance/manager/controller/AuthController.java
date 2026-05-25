package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.auth.GenericResponse;
import com.personalfinance.manager.dto.auth.LoginRequest;
import com.personalfinance.manager.dto.auth.RegisterRequest;
import com.personalfinance.manager.dto.auth.RegisterResponse;
import com.personalfinance.manager.dto.auth.UserResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.security.SecurityUtils;
import com.personalfinance.manager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityUtils securityUtils;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for username: {}", request.getUsername());
        RegisterResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse> login(@Valid @RequestBody LoginRequest request,
                                                 HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) {
        log.info("Received login request for username: {}", request.getUsername());

        // Authenticate credentials via AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Explicitly set the security context and persist in session repository
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpServletRequest, httpServletResponse);

        log.info("Login successful for username: {}", request.getUsername());
        return ResponseEntity.ok(GenericResponse.builder()
            .message("Login successful")
            .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse> logout(HttpServletRequest request) {
        log.info("Received logout request");
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("Session invalidated successfully.");
        }
        
        SecurityContextHolder.clearContext();
        log.info("Security context cleared.");

        return ResponseEntity.ok(GenericResponse.builder()
            .message("Logout successful")
            .build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Received request to fetch currently authenticated user");
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            log.warn("Fetch current user failed - no authenticated user found");
            throw new UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .build());
    }
}
