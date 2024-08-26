package com.app.CashLedger.services;

import com.app.CashLedger.dao.PaymentDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.*;
import com.app.CashLedger.dto.payment.SubscriptionDetails;
import com.app.CashLedger.dto.profile.PasswordUpdatePayload;
import com.app.CashLedger.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountServiceImpl implements UserAccountService{
    private final UserAccountDao userAccountDao;
    private final PasswordEncoder passwordEncoder;
    private final PaymentDao paymentDao;
    @Autowired
    public UserAccountServiceImpl(
            UserAccountDao userAccountDao,
            PasswordEncoder passwordEncoder,
            PaymentDao paymentDao
    ) {
        this.userAccountDao = userAccountDao;
        this.passwordEncoder = passwordEncoder;
        this.paymentDao = paymentDao;
    }
    @Transactional
    @Override
    public UserDetailsDto registerUser(RegistrationDetailsDto registrationDetailsDto) {
        UserAccount userAccount = UserAccount.builder()
                .fname(registrationDetailsDto.getFname())
                .lname(registrationDetailsDto.getLname())
                .email(registrationDetailsDto.getEmail())
                .phoneNumber(registrationDetailsDto.getPhoneNumber())
                .password(passwordEncoder.encode(registrationDetailsDto.getPassword()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .messages(new ArrayList<>())
                .transactions(new ArrayList<>())
                .build();
        UserAccount userAccount1 = userAccountDao.saveUser(userAccount);
        Payment payment = Payment.builder()
                .freeTrialStartedOn(LocalDateTime.now())
                .freeTrialEndedOn(LocalDateTime.now().plusDays(7))
                .userAccount(userAccount1)
                .build();
        paymentDao.makePayment(payment);
        return userAccountToUserDetailsDto(userAccount1);
    }
    @Transactional
    @Override
    public UserDetailsDto updateUser(RegistrationDetailsDto registrationDetailsDto, Integer userId) {
        UserAccount userAccount = userAccountDao.getUser(userId);
        userAccount.setEmail(registrationDetailsDto.getEmail());
        userAccount.setFname(registrationDetailsDto.getFname());
        userAccount.setLname(registrationDetailsDto.getLname());
        return userAccountToUserDetailsDto(userAccountDao.updateUser(userAccount));
    }
    @Transactional
    @Override
    public UserDetailsDto updatePassword(PasswordUpdatePayload passwordUpdatePayload) {
        System.out.println("UPDATING PASSWORD");
        System.out.println("PASSWORD" + passwordUpdatePayload.getNewPassword());
        UserAccount userAccount = userAccountDao.findByPhoneNumber(passwordUpdatePayload.getPhoneNumber());
        System.out.println("STUCK HERE-1");
        userAccount.setPassword(passwordEncoder.encode(passwordUpdatePayload.getNewPassword()));
        System.out.println("STUCK HERE-2");
        System.out.println(userAccount);
        UserAccount updatedUser = userAccountDao.updateUser(userAccount);  // Use the managed instance
        System.out.println("STUCK HERE-3");
        return userAccountToUserDetailsDto(updatedUser);
    }


    @Override
    public UserDetailsDto getUser(Integer userId) {
        return userAccountToUserDetailsDto(userAccountDao.getUser(userId));
    }

    @Override
    public List<UserDetailsDto> getUsers() {
        List<UserAccount> users = userAccountDao.getUsers();
        List<UserDetailsDto> transformedUsers = new ArrayList<>();
        for(UserAccount userAccount : users) {
            transformedUsers.add(userAccountToUserDetailsDto(userAccount));
        }
        return transformedUsers;
    }

    @Override
    public PaginatedResponse<UserDto> filterUsers(String name, String phoneNumber, Boolean orderByDate, String startDateStr, String endDateStr, int page, int size) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startDateStr, dateFormatter);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endDateStr, dateFormatter);
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }

        List<UserAccount> userAccounts = userAccountDao.filterUsers(name, phoneNumber, orderByDate, startDate, endDate, page, size);
        long totalUsers = userAccountDao.countFilteredUsers(name, phoneNumber, orderByDate, startDate, endDate);

        List<UserDto> userDtos = new ArrayList<>();
        for (UserAccount userAccount : userAccounts) {
            userDtos.add(userToUserDto(userAccount));
        }

        int totalPages = (int) Math.ceil((double) totalUsers / size);

        return new PaginatedResponse<>(userDtos, totalUsers, totalPages, page, size);
    }


    @Override
    public PaginatedResponse<UserDto> getActiveUsers(String name, String phoneNumber, Boolean orderByDate, String startDateStr, String endDateStr, int page, int size) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startDateStr, dateFormatter);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endDateStr, dateFormatter);
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }

        List<UserAccount> userAccounts = userAccountDao.getActiveUsers(name, phoneNumber, orderByDate, startDate, endDate, page, size);
        long totalUsers = userAccountDao.countActiveUsers(name, phoneNumber, orderByDate, startDate, endDate);

        List<UserDto> userDtos = new ArrayList<>();
        for (UserAccount userAccount : userAccounts) {
            userDtos.add(userToUserDto(userAccount));
        }

        int totalPages = (int) Math.ceil((double) totalUsers / size);

        return new PaginatedResponse<>(userDtos, totalUsers, totalPages, page, size);
    }

    public UserDto userToUserDto(UserAccount userAccount) {
        String name = "";
        if(userAccount.getFname() != null && userAccount.getLname() != null) {
            name = userAccount.getFname() + " " + userAccount.getLname();
        } else if(userAccount.getFname() != null) {
            name = userAccount.getFname();
        } else if(userAccount.getLname() != null) {
            name = userAccount.getLname();
        }
        return UserDto.builder()
                .userId(userAccount.getId())
                .name(name)
                .email(userAccount.getEmail())
                .phoneNumber(userAccount.getPhoneNumber())
                .createdOn(String.valueOf(userAccount.getCreatedAt()))
                .lastLogin(String.valueOf(userAccount.getLastLogin()))
                .transactionsSize(userAccount.getTransactions().size())
                .paymentsSize(userAccount.getPayments().size())
                .build();
    }

    private UserDetailsDto userAccountToUserDetailsDto(UserAccount userAccount) {
        List<Message> messages = userAccount.getMessages();
        List<Transaction> transactions = userAccount.getTransactions();
        List<Payment> payments = userAccount.getPayments();
        List<MessageDto> transformedMessages = new ArrayList<>();
        List<TransactionDto> transformedTransactions = new ArrayList<>();
        List<SubscriptionDetails> subscriptionDetails = new ArrayList<>();

        for(Message message : messages) {
            transformedMessages.add(messageToMessageDto(message));
        }

        for(Transaction transaction : transactions) {
            transformedTransactions.add(transactionToTransactionDto(transaction));
        }

        if(payments != null) {
            for(Payment payment : payments) {
                subscriptionDetails.add(paymentToPaymentDetails(payment));
            }
        }



        UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                .id(userAccount.getId())
                .fname(userAccount.getFname())
                .lname(userAccount.getLname())
                .email(userAccount.getEmail())
                .phoneNumber(userAccount.getPhoneNumber())
                .messages(transformedMessages)
                .transactions(transformedTransactions)
                .payments(subscriptionDetails)
                .createdAt(userAccount.getCreatedAt())
                .lastLogin(userAccount.getLastLogin())
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
        Boolean sessionExpired;
        String paidAt;
        String expiredAt;
        Integer userId;
        if(payment.getPaidAt() != null) {
            sessionExpired = payment.getPaidAt().isAfter(payment.getExpiredAt());
            paidAt = String.valueOf(payment.getPaidAt());
            expiredAt = String.valueOf(payment.getExpiredAt());
            userId = payment.getUserAccount().getId();
        } else {
            sessionExpired = true;
            paidAt = "";
            expiredAt = "";
            userId = 0;
        }
        SubscriptionDetails subscriptionDetails = SubscriptionDetails.builder()
                .id(payment.getId())
                .month(payment.getMonth())
                .paidAt(paidAt)
                .expiredAt(expiredAt)
                .sessionExpired(sessionExpired)
                .userId(userId)
                .build();
        return subscriptionDetails;
    }
}
