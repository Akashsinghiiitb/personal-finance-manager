package com.personalfinance.manager.dto.transaction;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionListResponse {
    private List<TransactionResponse> transactions;
}
