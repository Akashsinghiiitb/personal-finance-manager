package com.personalfinance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.manager.config.SecurityConfig;
import com.personalfinance.manager.dto.transaction.TransactionRequest;
import com.personalfinance.manager.dto.transaction.TransactionResponse;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.exception.BadRequestException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.security.CustomAccessDeniedHandler;
import com.personalfinance.manager.security.CustomAuthenticationEntryPoint;
import com.personalfinance.manager.security.UserDetailsServiceImpl;
import com.personalfinance.manager.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        transactionRequest = TransactionRequest.builder()
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category("Salary")
                .description("January Salary")
                .build();

        transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category("Salary")
                .description("January Salary")
                .type(TransactionType.INCOME)
                .build();
    }

    @Test
    @WithMockUser
    void createTransaction_success() throws Exception {
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(transactionResponse);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    @WithMockUser
    void createTransaction_validationError() throws Exception {
        TransactionRequest invalidRequest = TransactionRequest.builder()
                .amount(new BigDecimal("-10.00")) // negative amount not allowed in validation DTO
                .date(LocalDate.now().plusDays(1)) // future dates not allowed
                .category("")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser
    void getTransactions_success() throws Exception {
        when(transactionService.getTransactions(any(), any(), any()))
                .thenReturn(Collections.singletonList(transactionResponse));

        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value(1L));
    }

    @Test
    @WithMockUser
    void updateTransaction_success() throws Exception {
        when(transactionService.updateTransaction(eq(1L), any(TransactionRequest.class))).thenReturn(transactionResponse);

        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void updateTransaction_changeDate_throwsBadRequestException() throws Exception {
        when(transactionService.updateTransaction(eq(1L), any(TransactionRequest.class)))
                .thenThrow(new BadRequestException("Date field cannot be modified"));

        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Date field cannot be modified"));
    }

    @Test
    @WithMockUser
    void updateTransaction_notFound() throws Exception {
        when(transactionService.updateTransaction(eq(99L), any(TransactionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(put("/api/transactions/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    @Test
    @WithMockUser
    void deleteTransaction_success() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1L);

        mockMvc.perform(delete("/api/transactions/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
