package com.personalfinance.manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.manager.dto.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now().toString())
            .status(HttpServletResponse.SC_FORBIDDEN)
            .message("Forbidden")
            .details(Collections.singletonList(accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Access denied to the requested resource"))
            .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
