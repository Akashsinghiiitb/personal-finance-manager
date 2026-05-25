package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.goal.SavingsGoalRequest;
import com.personalfinance.manager.dto.goal.SavingsGoalResponse;
import com.personalfinance.manager.entity.SavingsGoal;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.BadRequestException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.repository.SavingsGoalRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User mockUser;
    private SavingsGoal mockGoal;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("user@example.com")
                .fullName("John Doe")
                .password("encodedPassword")
                .phoneNumber("+1234567890")
                .build();

        mockGoal = SavingsGoal.builder()
                .id(50L)
                .goalName("Car Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .startDate(LocalDate.now().minusMonths(1))
                .targetDate(LocalDate.now().plusYears(1))
                .user(mockUser)
                .build();
    }

    @Test
    void createGoal_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(mockGoal);
        when(transactionRepository.sumIncomeByUserIdAndDateAfter(anyLong(), any(LocalDate.class))).thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumExpenseByUserIdAndDateAfter(anyLong(), any(LocalDate.class))).thenReturn(new BigDecimal("1000.00"));

        SavingsGoalRequest request = SavingsGoalRequest.builder()
                .goalName("Car Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .startDate(LocalDate.now().minusMonths(1))
                .targetDate(LocalDate.now().plusYears(1))
                .build();

        SavingsGoalResponse response = savingsGoalService.createGoal(request);

        assertNotNull(response);
        assertEquals(mockGoal.getId(), response.getId());
        assertEquals(mockGoal.getGoalName(), response.getGoalName());
        // Progress: 3000 - 1000 = 2000
        assertEquals(new BigDecimal("2000.00"), response.getCurrentProgress());
        // Percentage: (2000 / 10000) * 100 = 20%
        assertEquals(new BigDecimal("20.0"), response.getProgressPercentage());
        // Remaining: 10000 - 2000 = 8000
        assertEquals(new BigDecimal("8000.00"), response.getRemainingAmount());
    }

    @Test
    void createGoal_negativeAmount_throwsBadRequestException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);

        SavingsGoalRequest request = SavingsGoalRequest.builder()
                .goalName("Car Fund")
                .targetAmount(new BigDecimal("-100.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .build();

        assertThrows(BadRequestException.class, () -> savingsGoalService.createGoal(request));
    }

    @Test
    void createGoal_pastTargetDate_throwsBadRequestException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);

        SavingsGoalRequest request = SavingsGoalRequest.builder()
                .goalName("Car Fund")
                .targetAmount(new BigDecimal("1000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().minusDays(1))
                .build();

        assertThrows(BadRequestException.class, () -> savingsGoalService.createGoal(request));
    }

    @Test
    void getGoals_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(savingsGoalRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(mockGoal));
        when(transactionRepository.sumIncomeByUserIdAndDateAfter(anyLong(), any(LocalDate.class))).thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumExpenseByUserIdAndDateAfter(anyLong(), any(LocalDate.class))).thenReturn(new BigDecimal("1000.00"));

        List<SavingsGoalResponse> results = savingsGoalService.getGoals();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(50L, results.get(0).getId());
        assertEquals(new BigDecimal("4000.00"), results.get(0).getCurrentProgress());
        assertEquals(new BigDecimal("40.0"), results.get(0).getProgressPercentage());
    }

    @Test
    void getGoalById_notFound_throwsResourceNotFoundException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(savingsGoalRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.getGoalById(99L));
    }

    @Test
    void updateGoal_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(savingsGoalRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(mockGoal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(mockGoal);
        when(transactionRepository.sumIncomeByUserIdAndDateAfter(anyLong(), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpenseByUserIdAndDateAfter(anyLong(), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        SavingsGoalRequest request = SavingsGoalRequest.builder()
                .goalName("Updated Car Fund")
                .targetAmount(new BigDecimal("12000.00"))
                .startDate(mockGoal.getStartDate())
                .targetDate(mockGoal.getTargetDate())
                .build();

        SavingsGoalResponse response = savingsGoalService.updateGoal(50L, request);

        assertNotNull(response);
        assertEquals("Updated Car Fund", mockGoal.getGoalName());
        assertEquals(new BigDecimal("12000.00"), mockGoal.getTargetAmount());
        verify(savingsGoalRepository, times(1)).save(mockGoal);
    }

    @Test
    void deleteGoal_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(savingsGoalRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(mockGoal));

        savingsGoalService.deleteGoal(50L);

        verify(savingsGoalRepository, times(1)).delete(mockGoal);
    }
}
