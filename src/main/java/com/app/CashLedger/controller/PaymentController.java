package com.app.CashLedger.controller;

import com.app.CashLedger.dto.payment.PaymentPayload;
import com.app.CashLedger.dto.payment.PaymentStatusPayload;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Map.of;
@RestController
@RequestMapping("/api/")
public class PaymentController {
    private final PaymentService paymentService;
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @GetMapping("subscription/status/{userId}")
    public ResponseEntity<Response> checkSubscriptionStatus(@PathVariable("userId") Integer userId) {
        return buildResponse("payment", paymentService.getSubscriptionStatus(userId), "Payment status checked", HttpStatus.OK);
    }

    @PostMapping("payment/ipn")
    public ResponseEntity<Response> getInstantPaymentNotification(@RequestBody Map<String, Object> ipn) {
        System.out.println("CHECKING PAYMENT STATUS");
        System.out.println(ipn);
        return buildResponse("ipn", ipn, "IPN received", HttpStatus.OK);
    }

    @PostMapping("subscription/pay")
    public ResponseEntity<Response> paySubscriptionFee(@RequestBody PaymentPayload paymentPayload) throws URISyntaxException, IOException, InterruptedException {
        return buildResponse("payment", paymentService.paySubscriptionFee(paymentPayload), "Payment initiated", HttpStatus.OK);
    }

    @PostMapping("subpayment/status")
    public ResponseEntity<Response> checkSubscriptionPaymentStatus(@RequestBody PaymentStatusPayload paymentStatusPayload) throws URISyntaxException, IOException, InterruptedException {
        return buildResponse("payment", paymentService.getSubscriptionFeePaymentStatus(paymentStatusPayload), "Payment status checked", HttpStatus.OK);
    }

    @GetMapping("payment/all")
    public ResponseEntity<Response> getPayments(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return buildResponse("payments", paymentService.getPayments(name, month, phoneNumber, startDate, endDate), "Payments fetched", HttpStatus.OK);
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
