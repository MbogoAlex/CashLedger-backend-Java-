package com.app.CashLedger.reportModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AllTransactionsReportModel {
    private String datetime;
    private String transactionType;
    private String category;
    private String entity;
    private String moneyIn;
    private String moneyOut;
    private String transactionCost;
}
