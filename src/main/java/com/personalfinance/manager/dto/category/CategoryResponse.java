package com.personalfinance.manager.dto.category;

import com.personalfinance.manager.entity.enums.TransactionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String name;
    private TransactionType type;
    private Boolean isCustom;
}
