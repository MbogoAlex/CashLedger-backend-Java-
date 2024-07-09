package com.app.CashLedger.controller;

import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static java.util.Map.of;

@RestController
@RequestMapping("/api/")
public class TransactionController {
    private final TransactionService transactionService;
    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping("transaction/{id}")
    public ResponseEntity<Response> getUserTransactions(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "latest") Boolean latest,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getUserTransactions(userId, entity, categoryId, transactionType, latest, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @GetMapping("transaction/sorted/{id}")
    public ResponseEntity<Response> getUserTransactionsSorted(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "moneyIn") Boolean moneyIn,
            @RequestParam(value = "orderByAmount") Boolean orderByAmount,
            @RequestParam(value = "ascendingOrder") Boolean ascendingOrder,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getUserTransactionsSorted(userId, entity, categoryId, transactionType, moneyIn, orderByAmount, ascendingOrder, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }
    @GetMapping("transaction/balance/{userId}")
    public ResponseEntity<Response> getCurrentBalance(@PathVariable("userId") Integer userId) {
        return buildResponse("balance", transactionService.getCurrentBalance(userId), "Balance fetched", HttpStatus.OK);
    }
    @GetMapping("transaction/sortfrequency/{id}")
    public ResponseEntity<Response> getUserTransactionsSortedByFrequency(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "moneyIn") Boolean moneyIn,
            @RequestParam(value = "ascendingOrder") Boolean ascendingOrder,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getUserTransactionsSortedByFrequency(userId, entity, categoryId, transactionType, moneyIn, ascendingOrder, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @GetMapping("transaction/outandin/{id}")
    public ResponseEntity<Response> getExpenditure(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "moneyIn") Boolean moneyIn,
            @RequestParam(value = "latest") Boolean latest,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getExpenditure(userId, entity, categoryId, transactionType, moneyIn, latest, startDate, endDate), "Transactions fetched", HttpStatus.OK);
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
