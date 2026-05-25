package com.personalfinance.manager.controller;

import com.personalfinance.manager.config.SecurityConfig;
import com.personalfinance.manager.dto.report.MonthlyReportResponse;
import com.personalfinance.manager.dto.report.YearlyReportResponse;
import com.personalfinance.manager.security.CustomAccessDeniedHandler;
import com.personalfinance.manager.security.CustomAuthenticationEntryPoint;
import com.personalfinance.manager.security.UserDetailsServiceImpl;
import com.personalfinance.manager.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    private MonthlyReportResponse monthlyReportResponse;
    private YearlyReportResponse yearlyReportResponse;

    @BeforeEach
    void setUp() {
        monthlyReportResponse = MonthlyReportResponse.builder()
                .month(1)
                .year(2024)
                .totalIncome(new HashMap<>())
                .totalExpenses(new HashMap<>())
                .netSavings(new BigDecimal("1500.00"))
                .build();

        yearlyReportResponse = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(new HashMap<>())
                .totalExpenses(new HashMap<>())
                .netSavings(new BigDecimal("18000.00"))
                .build();
    }

    @Test
    @WithMockUser
    void getMonthlyReport_success() throws Exception {
        when(reportService.getMonthlyReport(2024, 1)).thenReturn(monthlyReportResponse);

        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.netSavings").value(1500.00));
    }

    @Test
    void getMonthlyReport_unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getYearlyReport_success() throws Exception {
        when(reportService.getYearlyReport(2024)).thenReturn(yearlyReportResponse);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.netSavings").value(18000.00));
    }

    @Test
    void getYearlyReport_unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isUnauthorized());
    }
}
