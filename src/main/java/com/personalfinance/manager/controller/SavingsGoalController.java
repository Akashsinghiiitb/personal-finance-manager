package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.auth.GenericResponse;
import com.personalfinance.manager.dto.goal.SavingsGoalRequest;
import com.personalfinance.manager.dto.goal.SavingsGoalResponse;
import com.personalfinance.manager.dto.goal.SavingsGoalListResponse;
import com.personalfinance.manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        log.info("Received request to create savings goal: {}", request.getGoalName());
        SavingsGoalResponse response = savingsGoalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<SavingsGoalListResponse> getGoals() {
        log.info("Received request to fetch all savings goals");
        List<SavingsGoalResponse> responses = savingsGoalService.getGoals();
        return ResponseEntity.ok(SavingsGoalListResponse.builder()
            .goals(responses)
            .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoalById(@PathVariable Long id) {
        log.info("Received request to fetch savings goal with ID: {}", id);
        SavingsGoalResponse response = savingsGoalService.getGoalById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        log.info("Received request to update savings goal with ID: {}", id);
        SavingsGoalResponse response = savingsGoalService.updateGoal(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteGoal(@PathVariable Long id) {
        log.info("Received request to delete savings goal with ID: {}", id);
        savingsGoalService.deleteGoal(id);
        return ResponseEntity.ok(GenericResponse.builder()
            .message("Savings goal deleted successfully")
            .build());
    }
}
