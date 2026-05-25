package com.personalfinance.manager.mapper;

import com.personalfinance.manager.dto.goal.SavingsGoalResponse;
import com.personalfinance.manager.entity.SavingsGoal;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SavingsGoalMapper {

    public static SavingsGoalResponse toResponse(SavingsGoal goal, BigDecimal currentProgress) {
        if (goal == null) {
            return null;
        }

        BigDecimal targetAmount = goal.getTargetAmount();
        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                .multiply(new BigDecimal("100"))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
            
            if (progressPercentage.compareTo(BigDecimal.ZERO) < 0) {
                progressPercentage = BigDecimal.ZERO;
            }
        }

        return SavingsGoalResponse.builder()
            .id(goal.getId())
            .goalName(goal.getGoalName())
            .targetAmount(targetAmount)
            .targetDate(goal.getTargetDate())
            .startDate(goal.getStartDate())
            .currentProgress(currentProgress)
            .progressPercentage(progressPercentage)
            .remainingAmount(remainingAmount)
            .build();
    }
}
