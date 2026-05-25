package com.personalfinance.manager.dto.goal;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalListResponse {
    private List<SavingsGoalResponse> goals;
}
