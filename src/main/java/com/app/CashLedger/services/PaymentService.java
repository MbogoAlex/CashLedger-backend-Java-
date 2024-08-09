package com.app.CashLedger.services;

import com.app.CashLedger.dto.payment.PaymentPayload;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public interface PaymentService {
    public Boolean getSubscriptionStatus(Integer userId);

    public Map<String, Object> paySubscriptionFee(PaymentPayload paymentPayload) throws URISyntaxException, IOException, InterruptedException;
}
