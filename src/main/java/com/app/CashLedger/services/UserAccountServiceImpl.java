package com.app.CashLedger.services;

import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.dto.RegistrationDetailsDto;
import com.app.CashLedger.dto.TransactionDto;
import com.app.CashLedger.dto.UserDetailsDto;
import com.app.CashLedger.models.Message;
import com.app.CashLedger.models.Role;
import com.app.CashLedger.models.Transaction;
import com.app.CashLedger.models.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
public class UserAccountServiceImpl implements UserAccountService{
    private final UserAccountDao userAccountDao;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserAccountServiceImpl(
            UserAccountDao userAccountDao,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountDao = userAccountDao;
        this.passwordEncoder = passwordEncoder;
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
                .messages(new ArrayList<>())
                .transactions(new ArrayList<>())
                .build();
        return userAccountToUserDetailsDto(userAccountDao.saveUser(userAccount));
    }
    @Transactional
    @Override
    public UserDetailsDto updateUser(RegistrationDetailsDto registrationDetailsDto, Integer userId) {
        UserAccount userAccount = userAccountDao.getUser(userId);
        userAccount.setEmail(registrationDetailsDto.getEmail());
        userAccount.setFname(registrationDetailsDto.getFname());
        userAccount.setLname(registrationDetailsDto.getLname());
        userAccount.setPhoneNumber(registrationDetailsDto.getPhoneNumber());
        return userAccountToUserDetailsDto(userAccountDao.updateUser(userAccount));
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

    private UserDetailsDto userAccountToUserDetailsDto(UserAccount userAccount) {
        List<Message> messages = userAccount.getMessages();
        List<Transaction> transactions = userAccount.getTransactions();
        List<MessageDto> transformedMessages = new ArrayList<>();
        List<TransactionDto> transformedTransactions = new ArrayList<>();

        for(Message message : messages) {
            transformedMessages.add(messageToMessageDto(message));
        }

        for(Transaction transaction : transactions) {
            transformedTransactions.add(transactionToTransactionDto(transaction));
        }

        UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                .id(userAccount.getId())
                .fname(userAccount.getFname())
                .lname(userAccount.getLname())
                .email(userAccount.getEmail())
                .phoneNumber(userAccount.getPhoneNumber())
                .messages(transformedMessages)
                .transactions(transformedTransactions)
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
}
