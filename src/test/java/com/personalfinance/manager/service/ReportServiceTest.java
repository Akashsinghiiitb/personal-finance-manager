package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.report.MonthlyReportResponse;
import com.personalfinance.manager.dto.report.YearlyReportResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ReportService reportService;

    private User mockUser;
    private Category incomeCategory;
    private Category expenseCategory;
    private Transaction incomeTx;
    private Transaction expenseTx;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("user@example.com")
                .fullName("John Doe")
                .password("encodedPassword")
                .phoneNumber("+1234567890")
                .build();

        incomeCategory = Category.builder()
                .id(10L)
                .name("Salary")
                .type(TransactionType.INCOME)
                .isCustom(false)
                .build();

        expenseCategory = Category.builder()
                .id(20L)
                .name("Food")
                .type(TransactionType.EXPENSE)
                .isCustom(false)
                .build();

        incomeTx = Transaction.builder()
                .id(100L)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category(incomeCategory)
                .user(mockUser)
                .build();

        expenseTx = Transaction.builder()
                .id(101L)
                .amount(new BigDecimal("400.00"))
                .date(LocalDate.of(2024, 1, 20))
                .category(expenseCategory)
                .user(mockUser)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMonthlyReport_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(incomeTx, expenseTx));

        MonthlyReportResponse report = reportService.getMonthlyReport(2024, 1);

        assertNotNull(report);
        assertEquals(1, report.getMonth());
        assertEquals(2024, report.getYear());
        
        // Income validation
        assertEquals(1, report.getTotalIncome().size());
        assertEquals(new BigDecimal("3000.00"), report.getTotalIncome().get("Salary"));
        
        // Expense validation
        assertEquals(1, report.getTotalExpenses().size());
        assertEquals(new BigDecimal("400.00"), report.getTotalExpenses().get("Food"));
        
        // Net savings: 3000 - 400 = 2600
        assertEquals(new BigDecimal("2600.00"), report.getNetSavings());
    }

    @Test
    void getMonthlyReport_unauthenticated_throwsUnauthorizedException() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> reportService.getMonthlyReport(2024, 1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getYearlyReport_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(incomeTx, expenseTx));

        YearlyReportResponse report = reportService.getYearlyReport(2024);

        assertNotNull(report);
        assertEquals(2024, report.getYear());
        
        // Income validation
        assertEquals(1, report.getTotalIncome().size());
        assertEquals(new BigDecimal("3000.00"), report.getTotalIncome().get("Salary"));
        
        // Expense validation
        assertEquals(1, report.getTotalExpenses().size());
        assertEquals(new BigDecimal("400.00"), report.getTotalExpenses().get("Food"));
        
        // Net savings
        assertEquals(new BigDecimal("2600.00"), report.getNetSavings());
    }

    @Test
    void getYearlyReport_unauthenticated_throwsUnauthorizedException() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> reportService.getYearlyReport(2024));
    }
}
