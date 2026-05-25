package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends
        JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findAllByUserIdOrderByDateDescIdDesc(Long userId);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = com.personalfinance.manager.entity.enums.TransactionType.INCOME AND t.date >= :startDate")
    BigDecimal sumIncomeByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = com.personalfinance.manager.entity.enums.TransactionType.EXPENSE AND t.date >= :startDate")
    BigDecimal sumExpenseByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
