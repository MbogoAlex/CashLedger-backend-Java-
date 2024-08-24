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
public class PaymentDetailsDto {
    private Integer id;
    private String name;
    private Month month;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;
    private LocalDateTime freeTrialStartedOn;
    private LocalDateTime freeTrialEndedOn;
}
