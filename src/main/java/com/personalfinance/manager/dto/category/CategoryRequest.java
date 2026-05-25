package com.personalfinance.manager.dto.category;

import com.personalfinance.manager.entity.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    private String name;

    @NotNull(message = "Category type must not be null")
    private TransactionType type;
}
