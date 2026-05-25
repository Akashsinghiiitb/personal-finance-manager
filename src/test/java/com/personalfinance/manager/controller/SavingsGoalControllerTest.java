package com.personalfinance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.manager.config.SecurityConfig;
import com.personalfinance.manager.dto.goal.SavingsGoalRequest;
import com.personalfinance.manager.dto.goal.SavingsGoalResponse;
import com.personalfinance.manager.exception.BadRequestException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.security.CustomAccessDeniedHandler;
import com.personalfinance.manager.security.CustomAuthenticationEntryPoint;
import com.personalfinance.manager.security.UserDetailsServiceImpl;
import com.personalfinance.manager.service.SavingsGoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavingsGoalController.class)
@Import(SecurityConfig.class)
public class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavingsGoalService savingsGoalService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    private SavingsGoalRequest savingsGoalRequest;
    private SavingsGoalResponse savingsGoalResponse;

    @BeforeEach
    void setUp() {
        savingsGoalRequest = SavingsGoalRequest.builder()
                .goalName("Trip")
                .targetAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .build();

        savingsGoalResponse = SavingsGoalResponse.builder()
                .id(1L)
                .goalName("Trip")
                .targetAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .currentProgress(new BigDecimal("100.00"))
                .progressPercentage(new BigDecimal("5.00"))
                .remainingAmount(new BigDecimal("1900.00"))
                .build();
    }

    @Test
    @WithMockUser
    void createGoal_success() throws Exception {
        when(savingsGoalService.createGoal(any(SavingsGoalRequest.class))).thenReturn(savingsGoalResponse);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savingsGoalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.goalName").value("Trip"))
                .andExpect(jsonPath("$.progressPercentage").value(5.00));
    }

    @Test
    @WithMockUser
    void createGoal_validationError() throws Exception {
        SavingsGoalRequest invalidRequest = SavingsGoalRequest.builder()
                .goalName("") // Blank goal name
                .targetAmount(new BigDecimal("-100.00")) // Negative target amount
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .build();

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser
    void getGoals_success() throws Exception {
        when(savingsGoalService.getGoals()).thenReturn(Collections.singletonList(savingsGoalResponse));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].id").value(1L));
    }

    @Test
    @WithMockUser
    void getGoalById_success() throws Exception {
        when(savingsGoalService.getGoalById(1L)).thenReturn(savingsGoalResponse);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getGoalById_notFound() throws Exception {
        when(savingsGoalService.getGoalById(99L)).thenThrow(new ResourceNotFoundException("Goal not found"));

        mockMvc.perform(get("/api/goals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found"));
    }

    @Test
    @WithMockUser
    void updateGoal_success() throws Exception {
        when(savingsGoalService.updateGoal(eq(1L), any(SavingsGoalRequest.class))).thenReturn(savingsGoalResponse);

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savingsGoalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void updateGoal_invalidTargetDate_throwsBadRequestException() throws Exception {
        when(savingsGoalService.updateGoal(eq(1L), any(SavingsGoalRequest.class)))
                .thenThrow(new BadRequestException("Target date must be in the future"));

        // Use valid dates in request body to bypass Bean Validation, 
        // but have the mock service throw BadRequestException
        SavingsGoalRequest validBodyRequest = SavingsGoalRequest.builder()
                .goalName("Trip")
                .targetAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .build();

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBodyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Target date must be in the future"));
    }

    @Test
    @WithMockUser
    void deleteGoal_success() throws Exception {
        doNothing().when(savingsGoalService).deleteGoal(1L);

        mockMvc.perform(delete("/api/goals/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Savings goal deleted successfully"));
    }
}
