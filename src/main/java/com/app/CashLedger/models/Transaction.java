package com.app.CashLedger.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "idx_transaction_date", columnList = "date"),
        @Index(name = "idx_sender", columnList = "sender"),
        @Index(name = "idx_recipient", columnList = "recipient"),
        @Index(name = "idx_entity", columnList = "entity"),
        @Index(name = "idx_user_id", columnList = "user_id"),
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "transaction_category_mapping", // Changed join table name to avoid conflict
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<TransactionCategory> categories = new ArrayList<>();
}
