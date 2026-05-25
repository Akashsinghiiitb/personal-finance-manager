package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.transaction.TransactionRequest;
import com.personalfinance.manager.dto.transaction.TransactionResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.entity.enums.TransactionType;
import com.personalfinance.manager.exception.BadRequestException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.exception.UnauthorizedException;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import com.personalfinance.manager.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;
    private Category mockCategory;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("user@example.com")
                .fullName("John Doe")
                .password("encodedPassword")
                .phoneNumber("+1234567890")
                .build();

        mockCategory = Category.builder()
                .id(1L)
                .name("Food")
                .type(TransactionType.EXPENSE)
                .isCustom(false)
                .build();

        mockTransaction = Transaction.builder()
                .id(100L)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now())
                .description("Lunch")
                .category(mockCategory)
                .user(mockUser)
                .build();
    }

    @Test
    void createTransaction_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Food", 1L)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Food")).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now())
                .category("Food")
                .description("Lunch")
                .build();

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(mockTransaction.getId(), response.getId());
        assertEquals(mockTransaction.getAmount(), response.getAmount());
        assertEquals("Food", response.getCategory());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_unauthenticated_throwsUnauthorizedException() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now())
                .category("Food")
                .build();

        assertThrows(UnauthorizedException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_futureDate_throwsBadRequestException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now().plusDays(1))
                .category("Food")
                .build();

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_negativeAmount_throwsBadRequestException() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("-10.00"))
                .date(LocalDate.now())
                .category("Food")
                .build();

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTransactions_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Collections.singletonList(mockTransaction));

        List<TransactionResponse> results = transactionService.getTransactions(null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(mockTransaction.getId(), results.get(0).getId());
    }

    @Test
    void updateTransaction_changeDate_ignoresDate() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockTransaction));
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Food", 1L)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Food")).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        LocalDate originalDate = mockTransaction.getDate();
        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("60.00"))
                .date(originalDate.minusDays(2)) // Different date
                .category("Food")
                .build();

        TransactionResponse response = transactionService.updateTransaction(100L, request);

        assertNotNull(response);
        assertEquals(originalDate, mockTransaction.getDate());
    }

    @Test
    void updateTransaction_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockTransaction));
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Food", 1L)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUserIdIsNull("Food")).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("60.00"))
                .date(mockTransaction.getDate()) // same date
                .category("Food")
                .description("Updated lunch description")
                .build();

        TransactionResponse response = transactionService.updateTransaction(100L, request);

        assertNotNull(response);
        verify(transactionRepository, times(1)).save(mockTransaction);
        assertEquals(new BigDecimal("60.00"), mockTransaction.getAmount());
        assertEquals("Updated lunch description", mockTransaction.getDescription());
    }

    @Test
    void deleteTransaction_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockTransaction));

        transactionService.deleteTransaction(100L);

        verify(transactionRepository, times(1)).delete(mockTransaction);
    }
}
