package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.goal.SavingsGoalRequest;
import com.personalfinance.manager.dto.goal.SavingsGoalResponse;
import com.personalfinance.manager.entity.SavingsGoal;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.BadRequestException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.mapper.SavingsGoalMapper;
import com.personalfinance.manager.repository.SavingsGoalRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        if (request.getTargetAmount() == null || request.getTargetAmount().doubleValue() <= 0) {
            throw new BadRequestException("Target amount must be positive");
        }
        if (request.getTargetDate() == null || request.getTargetDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future or present");
        }
        if (request.getStartDate() == null) {
            throw new BadRequestException("Start date must not be null");
        }

        SavingsGoal goal = SavingsGoal.builder()
            .goalName(request.getGoalName().trim())
            .targetAmount(request.getTargetAmount())
            .targetDate(request.getTargetDate())
            .startDate(request.getStartDate())
            .user(user)
            .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        log.info("Successfully created savings goal for user {}: ID={}, name={}", user.getUsername(), saved.getId(), saved.getGoalName());
        
        BigDecimal progress = getProgressForUserAndDate(user.getId(), saved.getStartDate());
        return SavingsGoalMapper.toResponse(saved, progress);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoals() {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        List<SavingsGoal> goals = savingsGoalRepository.findAllByUserId(user.getId());
        return goals.stream()
            .map(goal -> {
                BigDecimal progress = getProgressForUserAndDate(user.getId(), goal.getStartDate());
                return SavingsGoalMapper.toResponse(goal, progress);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoalById(Long id) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found or access denied"));

        BigDecimal progress = getProgressForUserAndDate(user.getId(), goal.getStartDate());
        return SavingsGoalMapper.toResponse(goal, progress);
    }

    @Transactional
    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found or access denied"));

        if (request.getTargetAmount() != null && request.getTargetAmount().doubleValue() <= 0) {
            throw new BadRequestException("Target amount must be positive");
        }
        if (request.getTargetDate() != null && request.getTargetDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future or present");
        }

        if (request.getGoalName() != null) {
            goal.setGoalName(request.getGoalName().trim());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }
        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        log.info("Successfully updated savings goal for user {}: ID={}", user.getUsername(), updated.getId());

        BigDecimal progress = getProgressForUserAndDate(user.getId(), updated.getStartDate());
        return SavingsGoalMapper.toResponse(updated, progress);
    }

    @Transactional
    public void deleteGoal(Long id) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found or access denied"));

        savingsGoalRepository.delete(goal);
        log.info("Successfully deleted savings goal for user {}: ID={}", user.getUsername(), id);
    }

    private BigDecimal getProgressForUserAndDate(Long userId, LocalDate startDate) {
        BigDecimal income = transactionRepository.sumIncomeByUserIdAndDateAfter(userId, startDate);
        BigDecimal expense = transactionRepository.sumExpenseByUserIdAndDateAfter(userId, startDate);
        return income.subtract(expense);
    }
}
