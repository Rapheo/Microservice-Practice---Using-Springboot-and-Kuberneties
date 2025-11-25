package com.MicroservicePractice.OrderService.controller;

import com.MicroservicePractice.OrderService.OrderServiceConfig;
import com.MicroservicePractice.OrderService.entity.Order;
import com.MicroservicePractice.OrderService.model.OrderRequest;
import com.MicroservicePractice.OrderService.model.OrderResponse;
import com.MicroservicePractice.OrderService.model.PaymentMode;
import com.MicroservicePractice.OrderService.repository.OrderRepository;
import com.MicroservicePractice.OrderService.service.OrderService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.Charset.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.util.StreamUtils.*;

@SpringBootTest({"server.port= 0", "spring.cloud.config.enabled=false"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})
public class OrderControllerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @RegisterExtension
    static WireMockExtension wireMockServer
            = WireMockExtension.newInstance()
            .options(WireMockConfiguration
                    .wireMockConfig()
                    .port(8080))
            .build();

    private ObjectMapper objectMapper
            = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() throws IOException {
        getProductDetailsResponse();
        doPayment();
        getPaymentDetails();
        reduceQuantity();
    }

    private void reduceQuantity() {
        wireMockServer.stubFor(put(urlMatching("/product/reduceQuantity/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getPaymentDetails() throws IOException {
        wireMockServer.stubFor(get(urlMatching("/payment/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                OrderControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("/mock/GetPayment.json"),
                                defaultCharset()
                        ))));
    }

    private void doPayment() {
        wireMockServer.stubFor(post(urlEqualTo("/payment"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                ));
    }

    private void getProductDetailsResponse() throws IOException {
        wireMockServer.stubFor(get(urlEqualTo("/product/1"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                OrderControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("/mock/GetProduct.json"),
                                defaultCharset()
                        ))));
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .quantity(370)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(200)
                .build();
    }

    @Test
    public void test_WhenPlaceOrder_DoPayment_Success() throws Exception {
        //First Place Order
        //Get Order by Order Id from Db and Check
        //Check output

        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Customer")))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String orderId = mvcResult.getResponse().getContentAsString();

        Optional<Order> order = orderRepository.findById(Long.valueOf(orderId));
        assertTrue(order.isPresent());

        Order o = order.get();
        assertEquals(Long.parseLong(orderId), o.getId());
        assertEquals("PLACED", o.getOrderStatus());
        assertEquals(orderRequest.getTotalAmount(), o.getAmount());
        assertEquals(orderRequest.getQuantity(), o.getQuantity());
    }

    @Test
    public void test_WhenPlaceOrderWithWrongAccess_thenThrow403() throws Exception {
        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    public void test_WhenGetOrder_Success() throws Exception{
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/order/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Customer")))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();
        Order order = orderRepository.findById(1l).get();
        String expectedResponse = getOrderResponse(order);

        assertEquals(actualResponse, expectedResponse);
    }

    private String getOrderResponse(Order order) throws  Exception {
        OrderResponse.PaymentDetails paymentDetails
                = objectMapper.readValue(
                        copyToString(
                                OrderControllerTest.class.getClassLoader()
                                        .getResourceAsStream("/mock/GetPayment.json"),
                                        defaultCharset()
                        ), OrderResponse.PaymentDetails.class
        );

        paymentDetails.setPaymentStatus("SUCCESS");

        OrderResponse.ProductDetails productDetails
                = objectMapper.readValue(
                        copyToString(
                                OrderControllerTest.class.getClassLoader()
                                        .getResourceAsStream("/mock/GetProduct.json"),
                                defaultCharset()),
                        OrderResponse.ProductDetails.class
        );

        OrderResponse orderResponse
                = OrderResponse.builder()
                        .orderId(order.getId())
                        .orderStatus(order.getOrderStatus())
                        .orderDate(order.getOrderDate())
                        .paymentDetails(paymentDetails)
                        .productDetails(productDetails)
                        .amount(order.getAmount())
                        .build();

        return objectMapper.writeValueAsString(orderResponse);
    }

    @Test
    public void testWhen_GetOrder_Order_Not_Found() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/order/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();


    }


}