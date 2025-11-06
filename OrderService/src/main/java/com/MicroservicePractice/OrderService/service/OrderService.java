package com.MicroservicePractice.OrderService.service;

import com.MicroservicePractice.OrderService.model.OrderRequest;
import com.MicroservicePractice.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
