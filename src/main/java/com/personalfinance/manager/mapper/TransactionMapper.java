package com.personalfinance.manager.mapper;

import com.personalfinance.manager.dto.transaction.TransactionResponse;
import com.personalfinance.manager.entity.Transaction;

public class TransactionMapper {

    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionResponse.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .date(transaction.getDate())
            .description(transaction.getDescription())
            .type(transaction.getType())
            .category(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
            .build();
    }
}
