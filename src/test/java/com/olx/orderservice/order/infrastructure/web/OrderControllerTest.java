package com.olx.orderservice.order.infrastructure.web;

import com.olx.orderservice.order.application.dto.CreateOrderRequest;
import com.olx.orderservice.order.application.dto.ListOrderResponse;
import com.olx.orderservice.order.application.dto.OrderResponse;
import com.olx.orderservice.order.application.dto.StatusTransitionRequest;
import com.olx.orderservice.order.application.dto.UpdateOrderRequest;
import com.olx.orderservice.order.application.exception.IllegalStatusTransitionException;
import com.olx.orderservice.order.application.exception.OrderNotFoundException;
import com.olx.orderservice.order.application.service.OrderApplicationService;
import com.olx.orderservice.order.domain.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Integration style test for OrderController.
 * Uses SpringBootTest with a random port and Java HttpClient.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderApplicationService orderService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldReturn201WhenCreatingOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("Andi Wijaya", List.of(
            new CreateOrderRequest.CreateLineItem("Apple", 3, new BigDecimal("0.50"))
        ));

        OrderResponse response = new OrderResponse();
        response.setOrderId(UUID.randomUUID());
        response.setCustomerName("Andi Wijaya");
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("1.50"));

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.CREATED.value(), httpResponse.statusCode());
        assertEquals("Andi Wijaya",
            objectMapper.readTree(httpResponse.body()).get("customerName").asText());
    }

    @Test
    void shouldReturn200WhenGettingOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = new OrderResponse();
        response.setOrderId(orderId);
        response.setCustomerName("Andi");

        when(orderService.getOrder(orderId)).thenReturn(response);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId))
            .GET()
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.OK.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(orderId)).thenThrow(new OrderNotFoundException(orderId));

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId))
            .GET()
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.NOT_FOUND.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn200WhenListingOrders() throws Exception {
        ListOrderResponse listResponse = new ListOrderResponse(List.of(), 0, 20, 0, 0);
        when(orderService.listOrders(anyInt(), anyInt(), anyString())).thenReturn(listResponse);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders?page=0&size=20&sort=newest"))
            .GET()
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.OK.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn200WhenUpdatingOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest("Budi", List.of(
            new CreateOrderRequest.CreateLineItem("Orange", 2, new BigDecimal("1.00"))
        ));

        OrderResponse response = new OrderResponse();
        response.setOrderId(orderId);
        response.setCustomerName("Budi");

        when(orderService.updateOrder(any(UUID.class), any(UpdateOrderRequest.class)))
            .thenReturn(response);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.OK.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn200WhenTransitioningStatus() throws Exception {
        UUID orderId = UUID.randomUUID();
        StatusTransitionRequest request = new StatusTransitionRequest(OrderStatus.PAID, null);

        OrderResponse response = new OrderResponse();
        response.setOrderId(orderId);
        response.setStatus(OrderStatus.PAID);

        when(orderService.transitionStatus(any(UUID.class), any(StatusTransitionRequest.class)))
            .thenReturn(response);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId + "/status"))
            .header("Content-Type", "application/json")
            .method("PUT", HttpRequest.BodyPublishers.ofString(
                objectMapper.writeValueAsString(request)))
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.OK.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn409WhenIllegalTransition() throws Exception {
        UUID orderId = UUID.randomUUID();
        StatusTransitionRequest request = new StatusTransitionRequest(OrderStatus.SHIPPED, null);

        when(orderService.transitionStatus(any(UUID.class), any(StatusTransitionRequest.class)))
            .thenThrow(new IllegalStatusTransitionException(
                OrderStatus.CREATED, OrderStatus.SHIPPED));

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId + "/status"))
            .header("Content-Type", "application/json")
            .method("PUT", HttpRequest.BodyPublishers.ofString(
                objectMapper.writeValueAsString(request)))
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.CONFLICT.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn204WhenDeletingOrder() throws Exception {
        UUID orderId = UUID.randomUUID();

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId))
            .DELETE()
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.NO_CONTENT.value(), httpResponse.statusCode());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderNotFoundException(orderId))
            .when(orderService).deleteOrder(orderId);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/orders/" + orderId))
            .DELETE()
            .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.NOT_FOUND.value(), httpResponse.statusCode());
    }
}
