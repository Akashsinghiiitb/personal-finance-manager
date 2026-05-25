package com.personalfinance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.manager.config.SecurityConfig;
import com.personalfinance.manager.dto.category.CategoryListResponse;
import com.personalfinance.manager.dto.category.CategoryRequest;
import com.personalfinance.manager.dto.category.CategoryResponse;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.exception.ConflictException;
import com.personalfinance.manager.exception.ForbiddenException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.security.CustomAccessDeniedHandler;
import com.personalfinance.manager.security.CustomAuthenticationEntryPoint;
import com.personalfinance.manager.security.UserDetailsServiceImpl;
import com.personalfinance.manager.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryRequest = CategoryRequest.builder()
                .name("Taxes")
                .type(TransactionType.EXPENSE)
                .build();

        categoryResponse = CategoryResponse.builder()
                .name("Taxes")
                .type(TransactionType.EXPENSE)
                .isCustom(true)
                .build();
    }

    @Test
    @WithMockUser
    void getCategories_success() throws Exception {
        CategoryListResponse response = CategoryListResponse.builder()
                .categories(Collections.singletonList(categoryResponse))
                .build();

        when(categoryService.getCategories()).thenReturn(response);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Taxes"));
    }

    @Test
    void getCategories_unauthorized() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createCategory_success() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Taxes"))
                .andExpect(jsonPath("$.custom").value(true));
    }

    @Test
    @WithMockUser
    void createCategory_conflictDefault() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenThrow(new ConflictException("Category already exists as a default category"));

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category already exists as a default category"));
    }

    @Test
    @WithMockUser
    void createCategory_validationError() throws Exception {
        CategoryRequest invalidRequest = CategoryRequest.builder()
                .name("") // Empty category name
                .build();

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteCategory("Taxes");

        mockMvc.perform(delete("/api/categories/Taxes")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    @WithMockUser
    void deleteCategory_defaultCategory_throwsForbiddenException() throws Exception {
        doThrow(new ForbiddenException("Default categories cannot be deleted"))
                .when(categoryService).deleteCategory("Food");

        mockMvc.perform(delete("/api/categories/Food")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Default categories cannot be deleted"));
    }

    @Test
    @WithMockUser
    void deleteCategory_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found"))
                .when(categoryService).deleteCategory("Unknown");

        mockMvc.perform(delete("/api/categories/Unknown")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }
}
