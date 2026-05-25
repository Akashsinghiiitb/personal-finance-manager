package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.transaction.TransactionRequest;
import com.personalfinance.manager.dto.transaction.TransactionResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.BadRequestException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.mapper.TransactionMapper;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        if (request.getDate() == null || request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be a future date");
        }

        String categoryName = request.getCategory().trim();
        Category category = categoryRepository.findByNameIgnoreCaseAndUserId(categoryName, user.getId())
            .or(() -> categoryRepository.findByNameIgnoreCaseAndUserIdIsNull(categoryName))
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));

        Transaction transaction = Transaction.builder()
            .amount(request.getAmount())
            .date(request.getDate())
            .description(request.getDescription())
            .category(category)
            .user(user)
            .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Successfully created transaction for user {}: ID={}, amount={}", user.getUsername(), saved.getId(), saved.getAmount());
        return TransactionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(LocalDate startDate, LocalDate endDate, Long categoryId) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        Specification<Transaction> spec = Specification.where((root, query, cb) -> 
            cb.equal(root.get("user").get("id"), user.getId())
        );

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "date", "id");
        List<Transaction> transactions = transactionRepository.findAll(spec, sort);

        return transactions.stream()
            .map(TransactionMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or access denied"));

        if (request.getDate() != null && !request.getDate().equals(transaction.getDate())) {
            throw new BadRequestException("Transaction date cannot be updated");
        }
        if (request.getAmount() != null && request.getAmount().doubleValue() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        if (request.getCategory() != null) {
            String categoryName = request.getCategory().trim();
            Category category = categoryRepository.findByNameIgnoreCaseAndUserId(categoryName, user.getId())
                .or(() -> categoryRepository.findByNameIgnoreCaseAndUserIdIsNull(categoryName))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
            transaction.setCategory(category);
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        transaction.setDescription(request.getDescription()); // description can be updated to null/empty

        Transaction updated = transactionRepository.save(transaction);
        log.info("Successfully updated transaction for user {}: ID={}", user.getUsername(), updated.getId());
        return TransactionMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or access denied"));

        transactionRepository.delete(transaction);
        log.info("Successfully deleted transaction for user {}: ID={}", user.getUsername(), id);
    }
}
