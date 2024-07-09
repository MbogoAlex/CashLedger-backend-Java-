package com.app.CashLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BudgetResponseDto {
    private Integer id;
    private String name;
    private Double expenditure;
    private Double budgetLimit;
    private String createdAt;
    private String limitDate;
    private Boolean limitReached;
    private Double exceededBy;
    private Category category;
    private UserDetailsDto user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class Category {
        private Integer id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class UserDetailsDto {
        private Integer id;
        private String name;
    }

}
