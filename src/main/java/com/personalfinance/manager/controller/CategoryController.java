package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.auth.GenericResponse;
import com.personalfinance.manager.dto.category.CategoryListResponse;
import com.personalfinance.manager.dto.category.CategoryRequest;
import com.personalfinance.manager.dto.category.CategoryResponse;
import com.personalfinance.manager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CategoryListResponse> getCategories() {
        log.info("Received request to get all categories");
        CategoryListResponse response = categoryService.getCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("Received request to create category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<GenericResponse> deleteCategory(@PathVariable String name) {
        log.info("Received request to delete category: {}", name);
        categoryService.deleteCategory(name);
        return ResponseEntity.ok(GenericResponse.builder()
            .message("Category deleted successfully")
            .build());
    }
}
