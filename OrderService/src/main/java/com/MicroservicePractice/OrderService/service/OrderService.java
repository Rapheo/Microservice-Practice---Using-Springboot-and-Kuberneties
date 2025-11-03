package com.MicroservicePractice.OrderService.service;

import com.MicroservicePractice.OrderService.model.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
