package com.app.CashLedger.services;

import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.models.Message;
import java.util.List;

public interface MessageService {
    List<MessageDto> addMessages(List<MessageDto> messages, Integer userId);
    List<MessageDto> getMessages();

    List<MessageDto> processMessages(List<MessageDto> messages, Integer userId);
}
