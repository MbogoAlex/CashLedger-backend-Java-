package com.app.CashLedger.services;

import com.app.CashLedger.dto.BudgetEditDto;
import com.app.CashLedger.dto.BudgetResponseDto;
import java.util.List;

public interface BudgetService {
    BudgetResponseDto createBudget(BudgetEditDto budget, Integer userId, Integer categoryId);
    BudgetResponseDto updateBudget(BudgetEditDto budget, Integer budgetId);

    BudgetResponseDto getBudget(Integer budgetId);
    List<BudgetResponseDto> getUserBudgets(Integer userId, String name);

    List<BudgetResponseDto> getCategoryBudgets(Integer categoryId, String name);

    String deleteBudget(Integer budgetId);
}
