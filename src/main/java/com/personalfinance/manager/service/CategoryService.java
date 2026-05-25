package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.category.CategoryListResponse;
import com.personalfinance.manager.dto.category.CategoryRequest;
import com.personalfinance.manager.dto.category.CategoryResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.*;
import com.personalfinance.manager.mapper.CategoryMapper;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public CategoryListResponse getCategories() {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        log.info("Fetching categories for user: {}", user.getUsername());
        List<Category> categories = categoryRepository.findAllByUserIdOrUserIdIsNull(user.getId());

        List<CategoryResponse> categoryResponses = categories.stream()
            .map(CategoryMapper::toResponse)
            .collect(Collectors.toList());

        return CategoryListResponse.builder()
            .categories(categoryResponses)
            .build();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        String nameTrimmed = request.getName().trim();
        log.info("User {} requesting creation of custom category: {}", user.getUsername(), nameTrimmed);

        // Case-insensitive check: does it match global defaults?
        if (categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull(nameTrimmed)) {
            log.warn("Category creation failed: '{}' collides with a default global category", nameTrimmed);
            throw new ConflictException("Category already exists as a default category");
        }

        // Case-insensitive check: does it match user's custom categories?
        if (categoryRepository.existsByNameIgnoreCaseAndUserId(nameTrimmed, user.getId())) {
            log.warn("Category creation failed: '{}' collides with a custom category for this user", nameTrimmed);
            throw new ConflictException("Category already exists for this user");
        }

        Category category = Category.builder()
            .name(nameTrimmed)
            .type(request.getType())
            .isCustom(true)
            .user(user)
            .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Successfully created custom category '{}' for user {}", savedCategory.getName(), user.getUsername());
        
        return CategoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(String name) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        String nameTrimmed = name.trim();
        log.info("User {} requesting deletion of category: {}", user.getUsername(), nameTrimmed);

        // Check if category matches a global default category
        Optional<Category> globalCategoryOpt = categoryRepository.findByNameIgnoreCaseAndUserIdIsNull(nameTrimmed);
        if (globalCategoryOpt.isPresent()) {
            log.warn("Access Denied: Attempted deletion of global default category '{}' by user {}", nameTrimmed, user.getUsername());
            throw new ForbiddenException("Default categories cannot be deleted");
        }

        // Check if category matches custom category belonging to current user
        Category customCategory = categoryRepository.findByNameIgnoreCaseAndUserId(nameTrimmed, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found or you do not have permission to delete it"));

        // Check if custom category is referenced by any transactions
        if (transactionRepository.existsByCategoryId(customCategory.getId())) {
            log.warn("Category deletion failed: '{}' is referenced by active transactions", nameTrimmed);
            throw new BadRequestException("Categories referenced by transactions cannot be deleted");
        }

        categoryRepository.delete(customCategory);
        log.info("Successfully deleted custom category '{}' for user {}", customCategory.getName(), user.getUsername());
    }
}
