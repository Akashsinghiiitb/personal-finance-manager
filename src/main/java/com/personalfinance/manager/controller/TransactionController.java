package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.auth.GenericResponse;
import com.personalfinance.manager.dto.transaction.TransactionListResponse;
import com.personalfinance.manager.dto.transaction.TransactionRequest;
import com.personalfinance.manager.dto.transaction.TransactionResponse;
import com.personalfinance.manager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("Received request to create transaction of category: {}", request.getCategory());
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category) {
        log.info("Received request to get transactions with filters: startDate={}, endDate={}, categoryId={}, category={}",
                startDate, endDate, categoryId, category);
        List<TransactionResponse> responses;
        if (category != null) {
            responses = transactionService.getTransactions(startDate, endDate, categoryId, category);
        } else {
            responses = transactionService.getTransactions(startDate, endDate, categoryId);
        }
        return ResponseEntity.ok(TransactionListResponse.builder()
                .transactions(responses)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequest request) {
        log.info("Received request to update transaction with ID: {}", id);
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteTransaction(@PathVariable Long id) {
        log.info("Received request to delete transaction with ID: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(GenericResponse.builder()
                .message("Transaction deleted successfully")
                .build());
    }
}
