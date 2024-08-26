package com.app.CashLedger.controller;

import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.RegistrationDetailsDto;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.models.UserAccount;
import com.app.CashLedger.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static java.util.Map.of;

@RestController
@RequestMapping("/api/")
public class UserAccountController {
    private final UserAccountService userAccountService;
    private final UserAccountDao userAccountDao;
    @Autowired
    public UserAccountController(UserAccountService userAccountService, UserAccountDao userAccountDao) {
        this.userAccountService = userAccountService;
        this.userAccountDao = userAccountDao;
    }

    @PutMapping("user/{id}")
    public ResponseEntity<Response> updateUser(@RequestBody RegistrationDetailsDto registrationDetailsDto, @PathVariable("id") Integer userId) {
        return buildResponse("user", userAccountService.updateUser(registrationDetailsDto, userId), "User updated", HttpStatus.CREATED);
    }
    @GetMapping("user/{id}")
    public ResponseEntity<Response> getUser(@PathVariable("id") Integer userId) {
        return buildResponse("user", userAccountService.getUsers(), "User fetched", HttpStatus.OK);
    }
    @GetMapping("user/all")
    public ResponseEntity<Response> getUsers() {
        return buildResponse("user", userAccountService.getUsers(), "Users fetched", HttpStatus.OK);
    }

    @GetMapping("user/filter")
    public ResponseEntity<Response> filterUsers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "orderByDate") Boolean orderByDate,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("users", userAccountService.filterUsers(name, phoneNumber, orderByDate, startDate, endDate), "Users fetched", HttpStatus.OK);
    }

    @GetMapping("user/active")
    public ResponseEntity<Response> getActiveUsers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "orderByDate") Boolean orderByDate,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("users", userAccountService.getActiveUsers(name, phoneNumber, orderByDate, startDate, endDate), "Users fetched", HttpStatus.OK);
    }

    private ResponseEntity<Response> buildResponse(String desc, Object data, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Response.builder()
                        .timestamp(LocalDateTime.now())
                        .data(data == null ? null : of(desc, data))
                        .message(message)
                        .status(status)
                        .statusCode(status.value())
                        .build());
    }
}
