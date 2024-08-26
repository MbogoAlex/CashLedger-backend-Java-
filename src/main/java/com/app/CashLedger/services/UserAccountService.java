package com.app.CashLedger.services;

import com.app.CashLedger.dto.RegistrationDetailsDto;
import com.app.CashLedger.dto.UserDetailsDto;
import com.app.CashLedger.dto.UserDto;
import com.app.CashLedger.dto.profile.PasswordUpdatePayload;
import com.app.CashLedger.models.UserAccount;

import java.util.List;

public interface UserAccountService {
    UserDetailsDto registerUser(RegistrationDetailsDto registrationDetailsDto);

    UserDetailsDto updateUser(RegistrationDetailsDto registrationDetailsDto, Integer userId);

    UserDetailsDto updatePassword(PasswordUpdatePayload passwordUpdatePayload);

    UserDetailsDto getUser(Integer userId);

    List<UserDetailsDto> getUsers();
    List<UserDto> filterUsers(String name, String phoneNumber, Boolean orderByDate, String startDate, String endDate);
    List<UserDto> getActiveUsers(String name, String phoneNumber, Boolean orderByDate, String startDate, String endDate);
}
