package com.MicroservicePractice.PaymentService.service;

import com.MicroservicePractice.PaymentService.model.PaymentRequest;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);
}
