package com.app.CashLedger.services;

import com.app.CashLedger.dao.MessageDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.models.Message;
import com.app.CashLedger.models.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService{
    private final MessageDao messageDao;
    private final UserAccountDao userAccountDao;
    private final TransactionService transactionService;
    @Autowired
    public MessageServiceImpl(
            MessageDao messageDao,
            UserAccountDao userAccountDao,
            TransactionService transactionService
    ) {
        this.messageDao = messageDao;
        this.userAccountDao = userAccountDao;
        this.transactionService = transactionService;
    }
    @Transactional
    @Override
    public List<MessageDto> addMessages(List<MessageDto> messages, Integer userId) {
        System.out.println("RECEIVED " + messages.size() + " messages");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        UserAccount user = userAccountDao.getUser(userId);

        // Get existing messages for the user
        List<Message> existingMessages = messageDao.getMessagesById(userId);

        // Use a set for quick lookup of existing message bodies
        Set<String> existingMessageBodies = existingMessages.stream()
                .map(Message::getMessage)
                .collect(Collectors.toSet());

        // List to store new messages that are not duplicates
        List<MessageDto> newMessages = new ArrayList<>();

        for (MessageDto message : messages) {
            if (!existingMessageBodies.contains(message.getBody())) {
                newMessages.add(message);
            }
        }

        // Process and transform valid new messages to the entity format
        List<MessageDto> validMessages = processMessages(newMessages, userId);

        List<Message> messagesToAdd = new ArrayList<>();

        for (MessageDto messageDto : validMessages) {
            Message message = new Message();
            message.setMessage(messageDto.getBody());
            message.setDate(LocalDate.parse(messageDto.getDate(), formatter));
            message.setTime(LocalTime.parse(messageDto.getTime()));
            message.setUserAccount(user);
            messagesToAdd.add(message);
        }

        // Add the new messages to the database
        List<Message> addedMessages = messageDao.addMessages(messagesToAdd);

        // Transform added messages to DTO format for returning
        List<MessageDto> addedMessagesProcessed = new ArrayList<>();

        for (Message message : addedMessages) {
            addedMessagesProcessed.add(messageToMessageDto(message));
        }

        return addedMessagesProcessed;
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
    public List<MessageDto> processMessages(List<MessageDto> messages, Integer userId) {

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
                transactionService.extractTransactionDetails(messageDto, userId);
                processedMessages.add(messageDto);
            } catch (Exception e) {
                System.out.println("ERROR: "+e.toString());
                System.out.println(message);
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
