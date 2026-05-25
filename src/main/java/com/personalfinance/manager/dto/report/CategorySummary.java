package com.personalfinance.manager.dto.report;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySummary {
    private String categoryName;
    private BigDecimal amount;
}
