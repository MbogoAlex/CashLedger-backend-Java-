package com.app.CashLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Integer userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String createdOn;
    private Integer transactionsSize;
}
