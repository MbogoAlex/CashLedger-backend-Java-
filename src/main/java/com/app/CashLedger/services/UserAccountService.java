package com.app.CashLedger.services;

import com.app.CashLedger.dto.RegistrationDetailsDto;
import com.app.CashLedger.dto.UserDetailsDto;
import com.app.CashLedger.dto.profile.PasswordUpdatePayload;

import java.util.List;

public interface UserAccountService {
    UserDetailsDto registerUser(RegistrationDetailsDto registrationDetailsDto);

    UserDetailsDto updateUser(RegistrationDetailsDto registrationDetailsDto, Integer userId);

    UserDetailsDto updatePassword(PasswordUpdatePayload passwordUpdatePayload);

    UserDetailsDto getUser(Integer userId);

    List<UserDetailsDto> getUsers();
}
