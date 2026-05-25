package com.personalfinance.manager.mapper;

import com.personalfinance.manager.dto.category.CategoryResponse;
import com.personalfinance.manager.entity.Category;

public class CategoryMapper {
    
    public static CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
            .name(category.getName())
            .type(category.getType())
            .isCustom(category.isCustom())
            .build();
    }
}
