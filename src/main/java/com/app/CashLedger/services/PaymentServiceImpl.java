package com.app.CashLedger.services;

import com.app.CashLedger.dao.PaymentDao;
import com.app.CashLedger.dao.UserAccountDao;
import com.app.CashLedger.dto.payment.PaymentPayload;
import com.app.CashLedger.dto.payment.PaymentStatusPayload;
import com.app.CashLedger.models.Payment;
import com.app.CashLedger.models.UserAccount;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final PaymentDao paymentDao;
    private final UserAccountDao userAccountDao;
    @Autowired
    public PaymentServiceImpl(
            PaymentDao paymentDao,
            UserAccountDao userAccountDao
    ) {
        this.paymentDao = paymentDao;
        this.userAccountDao = userAccountDao;
    }

    private final String CONSUMER_KEY = "lM4HxeXzFtAyXOA0ABhG/r5pM6sYUnA6";
    private final String CONSUMER_SECRET = "sO+0e+mvfzKhHy0jFmkMCwufxG0=";

    private final String CALLBACK_URL = "https://github.com/MbogoAlex";
//    private final String notificationId = "5f1d6a44-c602-4232-80ba-dce3d1ec34fa";
    private final String notificationId = "f16139ae-0317-4142-bf77-dce0333014f0";
    @Override
    public Boolean getSubscriptionStatus(Integer userId) {
        List<Payment> payments = paymentDao.getLatestPayment(userId);

        if(payments != null && !payments.isEmpty()) {
            Payment payment = payments.get(0);
            return payment.getExpiredAt().isAfter(payment.getPaidAt());
        } else {
            return false;
        }
    }

    @Override
    public Map<String, Object> paySubscriptionFee(PaymentPayload paymentPayload) throws URISyntaxException, IOException, InterruptedException {
        String url = "https://pay.pesapal.com/v3/api/Transactions/SubmitOrderRequest";
        UserAccount userAccount = userAccountDao.getUser(paymentPayload.getUserId());
        String phoneNumber = paymentPayload.getPhoneNumber();
        String token = generateToken();

        String id = UUID.randomUUID().toString();

        Map<String, Object> billingAddress = new HashMap<>();
        billingAddress.put("email_address", Optional.ofNullable(userAccount.getEmail()).orElse(""));
        billingAddress.put("phone_number", phoneNumber);
        billingAddress.put("country_code", "KE");
        billingAddress.put("first_name", Optional.ofNullable(userAccount.getFname()).orElse(""));
        billingAddress.put("middle_name", "");
        billingAddress.put("last_name", Optional.ofNullable(userAccount.getLname()).orElse(""));
        billingAddress.put("line_1", "");
        billingAddress.put("line_2", "");
        billingAddress.put("city", "");
        billingAddress.put("state", "");
        billingAddress.put("postal_code", "");
        billingAddress.put("zip_code", "");

        Map<String, Object> payLoad = new HashMap<>();
        payLoad.put("id", id);
        payLoad.put("currency", "KES");
        payLoad.put("amount", "1");
        payLoad.put("description", "Subscription fee");
        payLoad.put("callback_url", CALLBACK_URL);
        payLoad.put("notification_id", notificationId);
        payLoad.put("billing_address", billingAddress);

        Gson gson = new Gson();



        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+token)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payLoad)))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        if (postResponse.statusCode() != HttpStatus.OK.value()) {
            throw new BadRequestException(token+"Failed to initialize payment::: Status code " + postResponse.statusCode() +" response body:: "+ postResponse.body());
        }

        String jsonString = postResponse.body();

        Map<String, Object> paymentResponse = gson.fromJson(jsonString, Map.class);
        paymentResponse.put("token", token);

        return paymentResponse;
    }

    @Override
    public Boolean getSubscriptionFeePaymentStatus(PaymentStatusPayload paymentStatusPayload) throws URISyntaxException, IOException, InterruptedException {
        System.out.println("CHECKING PAYMENT STATUS");
        Gson gson = new Gson();
        String orderId = paymentStatusPayload.getOrderId();
        String token = paymentStatusPayload.getToken();
        String url = "https://pay.pesapal.com/v3/api/Transactions/GetTransactionStatus?orderTrackingId="+orderId;

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        if(getResponse.statusCode() != HttpStatus.OK.value()) {
            return false;
        }

        String jsonString = getResponse.body();

        Map<String, Object> paymentResponse = gson.fromJson(jsonString, Map.class);
        if(paymentResponse.get("status").equals("200")) {
            return savePayment(paymentStatusPayload.getUserId());
        } else {
            return false;
        }
    }
//    @Transactional
    public Boolean savePayment(Integer userId) {
        System.out.println("Saving payment");
        UserAccount userAccount = userAccountDao.getUser(userId);
        Payment payment = Payment.builder()
                .month(LocalDateTime.now().getMonth())
                .paidAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMonths(1))
                .amount(100.0)
                .userAccount(userAccount)
                .build();
        System.out.println(payment);
        paymentDao.makePayment(payment);
        System.out.println("Payment saved");
        return true;
    }

    String generateToken() throws URISyntaxException, IOException, InterruptedException {
        String url = "https://pay.pesapal.com/v3/api/Auth/RequestToken";

        Map<String, Object> getTokenMap = new HashMap<>();
        getTokenMap.put("consumer_key", CONSUMER_KEY);
        getTokenMap.put("consumer_secret", CONSUMER_SECRET);

        Gson gson = new Gson();
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(getTokenMap)))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        if(postResponse.statusCode() != HttpStatus.OK.value()) {
            throw new BadRequestException("Failed to get token: Status code " + postResponse.statusCode() + "Response body:: "+postResponse.body());
        }

        String jsonString = postResponse.body();
        Map<String, Object> responseMap = gson.fromJson(jsonString, Map.class);

        return (String) responseMap.get("token");
    }




}
