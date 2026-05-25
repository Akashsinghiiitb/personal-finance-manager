package com.personalfinance.manager.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalRequest {

    @NotBlank(message = "Goal name must not be blank")
    private String goalName;

    @NotNull(message = "Target amount must not be null")
    @Positive(message = "Target amount must be a positive number greater than 0")
    private BigDecimal targetAmount;

    @NotNull(message = "Target date must not be null")
    @FutureOrPresent(message = "Target date must be today or in the future")
    private LocalDate targetDate;

    private LocalDate startDate;
}
