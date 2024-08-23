package com.app.CashLedger.services;

import com.app.CashLedger.dto.payment.PaymentDetailsDto;
import com.app.CashLedger.dto.payment.PaymentPayload;
import com.app.CashLedger.dto.payment.PaymentStatusPayload;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import java.util.List;

public interface PaymentService {
    public Boolean getSubscriptionStatus(Integer userId);

    public Map<String, Object> paySubscriptionFee(PaymentPayload paymentPayload) throws URISyntaxException, IOException, InterruptedException;

    public Boolean getSubscriptionFeePaymentStatus(PaymentStatusPayload paymentStatusPayload) throws URISyntaxException, IOException, InterruptedException;

    public List<PaymentDetailsDto> getPayments(String name, String month, String phoneNumber, String startDate, String endDate);
}
