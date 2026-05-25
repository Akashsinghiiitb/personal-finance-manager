package com.personalfinance.manager.dto.transaction;

import com.personalfinance.manager.entity.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private TransactionType type;
    private String category;
}
