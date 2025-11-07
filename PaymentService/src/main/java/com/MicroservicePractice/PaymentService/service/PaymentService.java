package com.MicroservicePractice.PaymentService.service;

import com.MicroservicePractice.PaymentService.model.PaymentRequest;
import com.MicroservicePractice.PaymentService.model.PaymentResponse;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
