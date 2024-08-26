package com.app.CashLedger.services;

import com.app.CashLedger.dto.PaginatedResponse;
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
    PaginatedResponse<UserDto> filterUsers(String name, String phoneNumber, Boolean orderByDate, String startDateStr, String endDateStr, int page, int size);
    PaginatedResponse<UserDto> getActiveUsers(String name, String phoneNumber, Boolean orderByDate, String startDateStr, String endDateStr, int page, int size);
}
