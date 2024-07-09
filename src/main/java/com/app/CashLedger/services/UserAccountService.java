package com.app.CashLedger.services;

import com.app.CashLedger.dto.RegistrationDetailsDto;
import com.app.CashLedger.dto.UserDetailsDto;
import java.util.List;

public interface UserAccountService {
    UserDetailsDto addUser(RegistrationDetailsDto registrationDetailsDto);

    UserDetailsDto updateUser(RegistrationDetailsDto registrationDetailsDto, Integer userId);

    UserDetailsDto getUser(Integer userId);

    List<UserDetailsDto> getUsers();
}
