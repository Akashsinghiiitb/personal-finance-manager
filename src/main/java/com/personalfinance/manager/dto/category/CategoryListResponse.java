package com.personalfinance.manager.dto.category;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryListResponse {
    private List<CategoryResponse> categories;
}
