package com.app.CashLedger.dto;
import com.app.CashLedger.models.CategoryKeyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransactionCategoryDto {
    private Integer id;
    private String name;
    private List<TransactionDto> transactions;
    private List<CategoryKeywordDto> keywords = new ArrayList<>();
    private List<BudgetDto> budgets = new ArrayList<>();
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class BudgetDto {
        private Integer id;
        private Double budgetLimit;
        private String createdAt;
        private String limitDate;
        private Boolean limitReached;
        private Double exceededBy;
    }
}
