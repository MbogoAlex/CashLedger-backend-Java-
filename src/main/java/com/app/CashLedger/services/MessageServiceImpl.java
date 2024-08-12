package com.app.CashLedger.services;

import com.app.CashLedger.dao.MessageDao;
import com.app.CashLedger.dao.TransactionDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.models.Message;
import com.app.CashLedger.models.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService{
    private final MessageDao messageDao;
    private final UserAccountDao userAccountDao;
    private final TransactionDao transactionDao;
    private final TransactionService transactionService;
    @Autowired
    public MessageServiceImpl(
            MessageDao messageDao,
            UserAccountDao userAccountDao,
            TransactionDao transactionDao,
            TransactionService transactionService
    ) {
        this.messageDao = messageDao;
        this.userAccountDao = userAccountDao;
        this.transactionDao = transactionDao;
        this.transactionService = transactionService;
    }

    @Override
    public List<MessageDto> addMessages(List<MessageDto> messages, Integer userId) {
        System.out.println("RECEIVED " + messages.size() + " messages");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        UserAccount user = userAccountDao.getUser(userId);
        return processMessages(messages, user);
    }


    @Override
    public List<MessageDto> getMessages() {
        List<Message> messages = messageDao.getMessages();
        List<MessageDto> transformedMessages = new ArrayList<>();
        for(Message message : messages) {
            transformedMessages.add(messageToMessageDto(message));
        }
        return transformedMessages;
    }

    @Override
    public List<MessageDto> processMessages(List<MessageDto> messages, UserAccount userAccount) {

        List<String> errMsg = new ArrayList<>();
        Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
                "sent to",
                "Withdraw Ksh",
                "paid to",
                "to Hustler Fund",
                "of airtime on",
                "of airtime for",
                "transferred to M-Shwari account",
                "transferred from M-Shwari",
                "Give Ksh",
                "from Hustler Fund",
                "You have received Ksh",
                "partially pay your outstanding Fuliza",
                "fully pay your outstanding Fuliza",
                "has been successfully reversed",
                "from your KCB M-PESA account",
                "transfered to KCB M-PESA account",
                "transfered to Lock Savings",
                "Your M-Shwari loan has been approved",
                "repaid from M-PESA",
                "from your M-PESA account to KCB M-PESA"
        ));

        List<String> notProcessed = new ArrayList<>();
        List<MessageDto> processedMessages = new ArrayList<>();
        for (MessageDto msgDto : messages) {
            String message = msgDto.getBody();
            String date = msgDto.getDate();
            String time = msgDto.getTime();

            if (time == null) {
                notProcessed.add(message);
                continue;
            }

            if (KEYWORDS.stream().noneMatch(message::contains)) {
                notProcessed.add(message);
                continue;
            }

            try {
                MessageDto messageDto = new MessageDto(message, date, time);
                transactionService.extractTransactionDetails(messageDto, userAccount);
                processedMessages.add(messageDto);
            } catch (Exception e) {
                errMsg.add(message);
            }
        }

        return processedMessages;

    }

    private MessageDto messageToMessageDto(Message message) {
        MessageDto messageDto = MessageDto.builder()
                .body(message.getMessage())
                .date(String.valueOf(message.getDate()))
                .time(String.valueOf(message.getTime()))
                .build();
        return messageDto;
    }
}