package com.app.CashLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BudgetEditDto {
    private String name;
    private Double budgetLimit;
    private String limitDate;
    private Boolean limitExceeded;
}
