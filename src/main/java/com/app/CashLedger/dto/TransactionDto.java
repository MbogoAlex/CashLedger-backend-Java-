package com.app.CashLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransactionDto {
    private Integer transactionId;
    private String transactionCode;
    private String transactionType;
    private Double transactionAmount;
    private Double transactionCost;
    private LocalDate date;
    private LocalTime time;
    private String sender;
    private String recipient;
    private String nickName;
    private String entity;
    private Double balance;
    private List<Category> categories = new ArrayList<>();
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class Category {
        private Integer id;
        private String name;
    }
}
