package com.app.CashLedger.controller;

import com.app.CashLedger.dto.BudgetEditDto;
import com.app.CashLedger.dto.BudgetResponseDto;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Map.of;

@RestController
@RequestMapping("/api/")
public class BudgetController {

    private final BudgetService budgetService;
    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }
    @PostMapping("budget/{userId}/{categoryId}")
    ResponseEntity<Response> createBudget(@RequestBody BudgetEditDto budget, @PathVariable("userId") Integer userId, @PathVariable("categoryId") Integer categoryId) {
        return buildResponse("budget", budgetService.createBudget(budget, userId, categoryId), "Budget created", HttpStatus.CREATED);
    }
    @PutMapping("budget/{userId}")
    ResponseEntity<Response> updateBudget(BudgetEditDto budget, Integer userId) {
        return buildResponse("budget", budgetService.updateBudget(budget, userId), "Budget updated", HttpStatus.OK);
    }
    @GetMapping("budget/single/{budgetId}")
    ResponseEntity<Response> getBudget(@PathVariable("budgetId") Integer budgetId) {
        return buildResponse("budget", budgetService.getBudget(budgetId), "Budget fetched successfully", HttpStatus.OK);
    }
    @GetMapping("budget/{userId}")
    ResponseEntity<Response> getUserBudgets(@PathVariable("userId") Integer userId) {
        return buildResponse("budget", budgetService.getUserBudgets(userId), "Budgets fetched", HttpStatus.OK);
    }
    @DeleteMapping("budget/{budgetId}")
    ResponseEntity<Response> deleteBudget(Integer budgetId) {
        return buildResponse("budget", budgetService.deleteBudget(budgetId), "Budget deleted", HttpStatus.OK);
    }

    private ResponseEntity<Response> buildResponse(String desc, Object data, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Response.builder()
                        .timestamp(LocalDateTime.now())
                        .data(data == null ? null : of(desc, data))
                        .message(message)
                        .status(status)
                        .statusCode(status.value())
                        .build());
    }
}
