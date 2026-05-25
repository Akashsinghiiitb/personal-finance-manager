package com.personalfinance.manager.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    public interface OnCreate {}
    public interface OnUpdate {}

    @NotNull(message = "Amount must not be null", groups = OnCreate.class)
    @Positive(message = "Amount must be a positive decimal greater than 0", groups = {OnCreate.class, OnUpdate.class})
    private BigDecimal amount;

    @NotNull(message = "Date must not be null", groups = OnCreate.class)
    @PastOrPresent(message = "Future date is not allowed", groups = {OnCreate.class, OnUpdate.class})
    private LocalDate date;

    @NotBlank(message = "Category name must not be blank", groups = OnCreate.class)
    private String category;

    private String description;
}
