package com.app.CashLedger.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Month;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionDetails {
    private Integer id;
    private Month month;
    private String paidAt;
    private String expiredAt;
    private Integer userId;
    private Boolean sessionExpired;
}
