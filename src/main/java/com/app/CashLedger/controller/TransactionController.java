package com.app.CashLedger.controller;

import com.app.CashLedger.dto.TransactionEditDto;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.reportModel.AllTransactionsReportModel;
import com.app.CashLedger.services.TransactionService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
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
            @RequestParam(value = "budgetId", required = false) Integer budgetId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "latest") Boolean latest,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getUserTransactions(userId, entity, categoryId, budgetId, transactionType, latest, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @GetMapping("transaction/sorted/{id}")
    public ResponseEntity<Response> getUserTransactionsSorted(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "budgetId", required = false) Integer budgetId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "moneyIn") Boolean moneyIn,
            @RequestParam(value = "orderByAmount") Boolean orderByAmount,
            @RequestParam(value = "ascendingOrder") Boolean ascendingOrder,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getUserTransactionsSorted(userId, entity, categoryId, budgetId, transactionType, moneyIn, orderByAmount, ascendingOrder, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @GetMapping("transaction/grouped/{id}")
    public ResponseEntity<Response> getGroupedByDateTransactions(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "budgetId", required = false) Integer budgetId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getGroupedByDateTransactions(userId, entity, categoryId, budgetId, transactionType, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @GetMapping("transaction/grouped/entity/{id}")
    public ResponseEntity<Response> getGroupedByEntityTransactions(
            @PathVariable("id") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "budgetId", required = false) Integer budgetId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getGroupedByEntityTransactions(userId, entity, categoryId, budgetId, transactionType, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @PutMapping("transaction/update")
    public ResponseEntity<Response> updateTransaction(@RequestBody TransactionEditDto transactionEditDto) {
        return buildResponse("transaction", transactionService.updateTransaction(transactionEditDto), "Transaction updated", HttpStatus.OK);
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
            @RequestParam(value = "budgetId", required = false) Integer budgetId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "moneyIn") Boolean moneyIn,
            @RequestParam(value = "latest") Boolean latest,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("transaction", transactionService.getExpenditure(userId, entity, categoryId, budgetId, transactionType, moneyIn, latest, startDate, endDate), "Transactions fetched", HttpStatus.OK);
    }

    @GetMapping("transaction/report/{userId}")
    public ResponseEntity<byte[]> generateAllTransactionsReport(
            @PathVariable("userId") Integer userId,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "budgetId", required = false) Integer budgetId,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) throws JRException, ParseException {
        byte[] pdfData = transactionService.generateAllTransactionsReport(userId, entity, categoryId, budgetId, transactionType, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "AllTransactionsReport.pdf");
        headers.setContentLength(pdfData.length);

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }
    @GetMapping("transaction/latest-code/{userId}")
    public ResponseEntity<Response> getLatestTransactionCodes(@PathVariable("userId") Integer userId) {
        System.out.println("GETTING LATEST TRANSACTION CODES");
        return buildResponse("transaction", transactionService.getLatestTransactionCode(userId), "Transaction codes fetched", HttpStatus.OK);
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
