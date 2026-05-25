package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.report.MonthlyReportResponse;
import com.personalfinance.manager.dto.report.YearlyReportResponse;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        log.info("Generating monthly report for user {} in range {} to {}", user.getUsername(), start, end);

        Specification<Transaction> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("user").get("id"), user.getId())
        );
        spec = spec.and(
            (root, query, cb) -> cb.between(root.get("date"), start, end)
        );

        List<Transaction> transactions = transactionRepository.findAll(spec);

        BigDecimal totalIncomeVal = BigDecimal.ZERO;
        BigDecimal totalExpensesVal = BigDecimal.ZERO;

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        for (Transaction t : transactions) {
            BigDecimal amt = t.getAmount();
            String catName = t.getCategory().getName();
            TransactionType type = t.getType();

            if (type == TransactionType.INCOME) {
                totalIncomeVal = totalIncomeVal.add(amt);
                incomeMap.put(catName, incomeMap.getOrDefault(catName, BigDecimal.ZERO).add(amt));
            } else if (type == TransactionType.EXPENSE) {
                totalExpensesVal = totalExpensesVal.add(amt);
                expenseMap.put(catName, expenseMap.getOrDefault(catName, BigDecimal.ZERO).add(amt));
            }
        }

        return MonthlyReportResponse.builder()
            .month(month)
            .year(year)
            .totalIncome(incomeMap)
            .totalExpenses(expenseMap)
            .netSavings(totalIncomeVal.subtract(totalExpensesVal))
            .build();
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(int year) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        log.info("Generating yearly report for user {} in range {} to {}", user.getUsername(), start, end);

        Specification<Transaction> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("user").get("id"), user.getId())
        );
        spec = spec.and(
            (root, query, cb) -> cb.between(root.get("date"), start, end)
        );

        List<Transaction> transactions = transactionRepository.findAll(spec);

        BigDecimal totalIncomeVal = BigDecimal.ZERO;
        BigDecimal totalExpensesVal = BigDecimal.ZERO;

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        for (Transaction t : transactions) {
            BigDecimal amt = t.getAmount();
            String catName = t.getCategory().getName();
            TransactionType type = t.getType();

            if (type == TransactionType.INCOME) {
                totalIncomeVal = totalIncomeVal.add(amt);
                incomeMap.put(catName, incomeMap.getOrDefault(catName, BigDecimal.ZERO).add(amt));
            } else if (type == TransactionType.EXPENSE) {
                totalExpensesVal = totalExpensesVal.add(amt);
                expenseMap.put(catName, expenseMap.getOrDefault(catName, BigDecimal.ZERO).add(amt));
            }
        }

        return YearlyReportResponse.builder()
            .year(year)
            .totalIncome(incomeMap)
            .totalExpenses(expenseMap)
            .netSavings(totalIncomeVal.subtract(totalExpensesVal))
            .build();
    }
}
