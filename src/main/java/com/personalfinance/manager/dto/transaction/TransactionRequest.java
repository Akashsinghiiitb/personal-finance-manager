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

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be a positive decimal greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Date must not be null")
    @PastOrPresent(message = "Future date is not allowed")
    private LocalDate date;

    @NotBlank(message = "Category name must not be blank")
    private String category;

    private String description;
}
