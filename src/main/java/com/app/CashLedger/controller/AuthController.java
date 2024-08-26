package com.app.CashLedger.controller;

import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.*;
import com.app.CashLedger.dto.payment.SubscriptionDetails;
import com.app.CashLedger.dto.profile.PasswordUpdatePayload;
import com.app.CashLedger.models.*;
import com.app.CashLedger.security.JWTGenerator;
import com.app.CashLedger.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;
@RestController
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserAccountService userAccountService;
    private final UserAccountDao userAccountDao;
    private final JWTGenerator jwtGenerator;
    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserAccountService userAccountService, UserAccountDao userAccountDao, JWTGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.userAccountService = userAccountService;
        this.userAccountDao = userAccountDao;
        this.jwtGenerator = jwtGenerator;
    }
    @PostMapping("register")
    public ResponseEntity<Response> register(@RequestBody RegistrationDetailsDto registrationDetailsDto) {
        if(userAccountDao.existsByPhoneNumber(registrationDetailsDto.getPhoneNumber())) {
            return buildResponse(null,null, "User exists", HttpStatus.CONFLICT);
        }
        return buildResponse("user", userAccountService.registerUser(registrationDetailsDto), "User added", HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<Response> login(@RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getPhoneNumber(),
                            loginDto.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            System.out.println("PHONE NUMBER:");
            System.out.println(loginDto.getPhoneNumber());

            UserAccount user = userAccountDao.findByPhoneNumber(loginDto.getPhoneNumber());


            Map<String, Object> tokenAndUserMap = new HashMap<>();
            tokenAndUserMap.put("token", token);
            tokenAndUserMap.put("userInfo", userAccountToUserDetailsDto(user));

            return buildResponse("user", tokenAndUserMap, "Login successful", HttpStatus.OK);
        } catch (AuthenticationException e) {
            return buildResponse(null, null, "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
    @PutMapping("update/password")
    public ResponseEntity<Response> updatePassword(@RequestBody PasswordUpdatePayload passwordUpdatePayload) {
        try {
            UserAccount user = userAccountDao.findByPhoneNumber(passwordUpdatePayload.getPhoneNumber());
            System.out.println(user.getFname());
            return buildResponse("user", userAccountService.updatePassword(passwordUpdatePayload), "Password updated", HttpStatus.OK);
        } catch (Exception e) {
            return buildResponse(null, null, "Invalid phone number", HttpStatus.UNAUTHORIZED);
        }
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

    private UserDetailsDto userAccountToUserDetailsDto(UserAccount userAccount) {
        List<Message> messages = userAccount.getMessages();
        List<Transaction> transactions = userAccount.getTransactions();
        List<MessageDto> transformedMessages = new ArrayList<>();
        List<TransactionDto> transformedTransactions = new ArrayList<>();
        List<SubscriptionDetails> subscriptionDetails = new ArrayList<>();

//        for(Message message : messages) {
//            transformedMessages.add(messageToMessageDto(message));
//        }

//        if(userAccount.getPayments() != null) {
//            for(Payment payment : userAccount.getPayments()) {
//                subscriptionDetails.add(paymentToPaymentDetails(payment));
//            }
//        }
//
//        for(Transaction transaction : transactions) {
//            transformedTransactions.add(transactionToTransactionDto(transaction));
//        }



        UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                .id(userAccount.getId())
                .fname(userAccount.getFname())
                .lname(userAccount.getLname())
                .email(userAccount.getEmail())
                .phoneNumber(userAccount.getPhoneNumber())
                .messages(new ArrayList<>())
                .transactions(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();
        return userDetailsDto;
    }

    private MessageDto messageToMessageDto(Message message) {
        MessageDto messageDto = MessageDto.builder()
                .body(message.getMessage())
                .date(String.valueOf(message.getDate()))
                .time(String.valueOf(message.getTime()))
                .build();
        return messageDto;
    }

    private TransactionDto transactionToTransactionDto(Transaction transaction) {
        TransactionDto transactionDto = TransactionDto.builder()
                .transactionCode(transaction.getTransactionCode())
                .transactionType(transaction.getTransactionType())
                .transactionAmount(transaction.getTransactionAmount())
                .transactionCost(transaction.getTransactionCost())
                .date(transaction.getDate())
                .time(transaction.getTime())
                .sender(transaction.getSender())
                .recipient(transaction.getRecipient())
                .entity(transaction.getEntity())
                .balance(transaction.getBalance())
                .build();
        return transactionDto;
    }

    SubscriptionDetails paymentToPaymentDetails(Payment payment) {
        SubscriptionDetails subscriptionDetails = SubscriptionDetails.builder()
                .id(payment.getId())
                .month(payment.getMonth())
                .paidAt(String.valueOf(payment.getPaidAt()))
                .expiredAt(String.valueOf(payment.getExpiredAt()))
                .sessionExpired(payment.getPaidAt().isAfter(payment.getExpiredAt()))
                .userId(payment.getUserAccount().getId())
                .build();
        return subscriptionDetails;
    }
}
