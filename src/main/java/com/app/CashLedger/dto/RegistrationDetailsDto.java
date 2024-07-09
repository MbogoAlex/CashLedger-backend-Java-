package com.app.CashLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDetailsDto {
    private String fname;
    private String lname;
    private String email;
    private String phoneNumber;

}
