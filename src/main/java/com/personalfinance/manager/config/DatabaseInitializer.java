package com.personalfinance.manager.config;

import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database initialization (seeding default categories)...");

        // Seed Income categories
        seedGlobalCategory("Salary", TransactionType.INCOME);

        // Seed Expense categories
        List<String> expenseCategories = Arrays.asList(
            "Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities"
        );
        for (String name : expenseCategories) {
            seedGlobalCategory(name, TransactionType.EXPENSE);
        }

        log.info("Database initialization completed successfully.");
    }

    private void seedGlobalCategory(String name, TransactionType type) {
        if (!categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull(name)) {
            Category category = Category.builder()
                .name(name)
                .type(type)
                .isCustom(false)
                .user(null)
                .build();
            categoryRepository.save(category);
            log.info("Seeded global category: {} ({})", name, type);
        } else {
            log.info("Global category already exists (skipping seed): {}", name);
        }
    }
}
