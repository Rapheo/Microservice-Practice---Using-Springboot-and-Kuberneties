package com.MicroservicePractice.OrderService.service.serviceImpl;

import com.MicroservicePractice.OrderService.entity.Order;
import com.MicroservicePractice.OrderService.exception.CustomException;
import com.MicroservicePractice.OrderService.external.client.PaymentService;
import com.MicroservicePractice.OrderService.external.client.ProductService;
import com.MicroservicePractice.OrderService.external.request.PaymentRequest;
import com.MicroservicePractice.OrderService.external.response.PaymentResponse;
import com.MicroservicePractice.OrderService.external.response.ProductResponse;
import com.MicroservicePractice.OrderService.model.OrderRequest;
import com.MicroservicePractice.OrderService.model.OrderResponse;
import com.MicroservicePractice.OrderService.repository.OrderRepository;
import com.MicroservicePractice.OrderService.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservices.product}")
    private String productServiceURL;

    @Value("${microservices.payment}")
    private String paymentServiceURL;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing Order Request: " + orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");

        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");
        PaymentRequest paymentRequest
                = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the Order Status to PAID");
            orderStatus = "PLACED";
        }catch (Exception ex){
            log.info("Error during Payment. Changing the Order Status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        order = orderRepository.save(order);

        log.info("Order Placed successfully with Order Id: {} ", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for Order Id: {}", orderId);

        Order order
                = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the OrderId: " + orderId, "NOT_FOUND", 404));

        log.info("Invoking Product service to deatch the product for orderId: {}", order.getId());

        //calling with restTemplate instead of Feign Client
        ProductResponse productResponse
                = restTemplate.getForObject(productServiceURL + order.getProductId(), ProductResponse.class);

        log.info("Getting Payment response for orderId: {} ", order.getId());
        PaymentResponse paymentResponse
                = restTemplate.getForObject(paymentServiceURL + "order/" + order.getId(), PaymentResponse.class);

        assert productResponse != null;
        OrderResponse.ProductDetails productDetails
                = OrderResponse.ProductDetails
                .builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .price(productResponse.getPrice())
                .build();

        OrderResponse.PaymentDetails paymentDetails
                = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentStatus(paymentResponse.getStatus())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();

        OrderResponse orderResponse
                = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }
}
