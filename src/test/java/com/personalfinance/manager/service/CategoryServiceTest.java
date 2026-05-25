package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.category.CategoryListResponse;
import com.personalfinance.manager.dto.category.CategoryRequest;
import com.personalfinance.manager.dto.category.CategoryResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.exception.*;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CategoryService categoryService;

    private User mockUser;
    private Category globalCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("user@example.com")
                .fullName("John Doe")
                .password("encodedPassword")
                .phoneNumber("+1234567890")
                .build();

        globalCategory = Category.builder()
                .id(10L)
                .name("Food")
                .type(TransactionType.EXPENSE)
                .isCustom(false)
                .user(null)
                .build();

        customCategory = Category.builder()
                .id(20L)
                .name("Taxes")
                .type(TransactionType.EXPENSE)
                .isCustom(true)
                .user(mockUser)
                .build();
    }

    @Test
    void getCategories_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.findAllByUserIdOrUserIdIsNull(1L))
                .thenReturn(Arrays.asList(globalCategory, customCategory));

        CategoryListResponse response = categoryService.getCategories();

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
        assertEquals("Food", response.getCategories().get(0).getName());
        assertFalse(response.getCategories().get(0).getIsCustom());
        assertEquals("Taxes", response.getCategories().get(1).getName());
        assertTrue(response.getCategories().get(1).getIsCustom());
    }

    @Test
    void getCategories_unauthenticated_throwsUnauthorizedException() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> categoryService.getCategories());
    }

    @Test
    void createCategory_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Taxes")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Taxes", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryRequest request = CategoryRequest.builder()
                .name("Taxes")
                .type(TransactionType.EXPENSE)
                .build();

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Taxes", response.getName());
        assertTrue(response.getIsCustom());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_unauthenticated_throwsUnauthorizedException() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        CategoryRequest request = CategoryRequest.builder()
                .name("Taxes")
                .type(TransactionType.EXPENSE)
                .build();

        assertThrows(UnauthorizedException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_collidesWithDefault_throwsConflictException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Food")).thenReturn(true);

        CategoryRequest request = CategoryRequest.builder()
                .name("Food")
                .type(TransactionType.EXPENSE)
                .build();

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_collidesWithCustom_throwsConflictException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Taxes")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Taxes", 1L)).thenReturn(true);

        CategoryRequest request = CategoryRequest.builder()
                .name("Taxes")
                .type(TransactionType.EXPENSE)
                .build();

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void deleteCategory_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Taxes")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Taxes", 1L)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(20L)).thenReturn(false);

        categoryService.deleteCategory("Taxes");

        verify(categoryRepository, times(1)).delete(customCategory);
    }

    @Test
    void deleteCategory_unauthenticated_throwsUnauthorizedException() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> categoryService.deleteCategory("Taxes"));
    }

    @Test
    void deleteCategory_defaultCategory_throwsForbiddenException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Food")).thenReturn(Optional.of(globalCategory));

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory("Food"));
    }

    @Test
    void deleteCategory_notFound_throwsResourceNotFoundException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Unknown")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Unknown", 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory("Unknown"));
    }

    @Test
    void deleteCategory_referencedByTransactions_throwsBadRequestException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Taxes")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Taxes", 1L)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(20L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("Taxes"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
