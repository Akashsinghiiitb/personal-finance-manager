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

    public interface OnCreate {}
    public interface OnUpdate {}

    @NotBlank(message = "Goal name must not be blank", groups = OnCreate.class)
    private String goalName;

    @NotNull(message = "Target amount must not be null", groups = OnCreate.class)
    @Positive(message = "Target amount must be a positive number greater than 0", groups = {OnCreate.class, OnUpdate.class})
    private BigDecimal targetAmount;

    @NotNull(message = "Target date must not be null", groups = OnCreate.class)
    @FutureOrPresent(message = "Target date must be today or in the future", groups = {OnCreate.class, OnUpdate.class})
    private LocalDate targetDate;

    private LocalDate startDate;
}
