package com.app.CashLedger.dto;

import com.app.CashLedger.dto.payment.SubscriptionDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserDetailsDto {
    private Integer id;
    private String fname;
    private String lname;
    private String email;
    private String phoneNumber;
    private List<MessageDto> messages;
    private List<TransactionDto> transactions;
    private List<SubscriptionDetails> payments;
}
