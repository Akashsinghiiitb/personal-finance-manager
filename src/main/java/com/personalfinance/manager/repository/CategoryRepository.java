package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCaseAndUserId(String name, Long userId);
    Optional<Category> findByNameIgnoreCaseAndUserIdIsNull(String name);
    List<Category> findAllByUserIdOrUserIdIsNull(Long userId);
    
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
    boolean existsByNameIgnoreCaseAndUserIdIsNull(String name);
}
