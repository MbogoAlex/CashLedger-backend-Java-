package com.app.CashLedger.controller;

import com.app.CashLedger.dto.MessageDto;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static java.util.Map.of;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class MessageController {
    private final MessageService messageService;
    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    @PostMapping("message/{id}")
    public ResponseEntity<Response> addMessages(@RequestBody List<MessageDto> messages, @PathVariable("id") Integer userId) {
        System.out.println("POSTING MESSAGES");
        return buildResponse("message", messageService.addMessages(messages, userId), "Messages added", HttpStatus.CREATED);
    }
    @GetMapping("message")
    public ResponseEntity<Response> getMessages() {
        return buildResponse("message", messageService.getMessages(), "Messages fetched", HttpStatus.OK);
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
