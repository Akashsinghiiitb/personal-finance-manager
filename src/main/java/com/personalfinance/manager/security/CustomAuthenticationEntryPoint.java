package com.personalfinance.manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.manager.dto.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now().toString())
            .status(HttpServletResponse.SC_UNAUTHORIZED)
            .message("Unauthorized")
            .details(Collections.singletonList(authException.getMessage() != null ? authException.getMessage() : "Full authentication is required to access this resource"))
            .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
