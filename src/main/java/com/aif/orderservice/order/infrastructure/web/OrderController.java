package com.aif.orderservice.order.infrastructure.web;

import com.aif.orderservice.order.application.dto.CreateOrderRequest;
import com.aif.orderservice.order.application.dto.ListOrderResponse;
import com.aif.orderservice.order.application.dto.OrderResponse;
import com.aif.orderservice.order.application.dto.StatusTransitionRequest;
import com.aif.orderservice.order.application.dto.UpdateOrderRequest;
import com.aif.orderservice.order.application.service.OrderApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller exposing order operations.
 * Maps HTTP requests to application service use cases.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderService;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order.
     * Returns 201 Created with the order body.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an order by its UUID.
     * Returns 200 OK with the order body, or 404 Not Found.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists orders with pagination and sorting.
     * Returns 200 OK with a paginated response.
     */
    @GetMapping
    public ResponseEntity<ListOrderResponse> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        ListOrderResponse response = orderService.listOrders(page, size, sort);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing order's customer name and items.
     * Returns 200 OK with the updated order, or 400/404 on error.
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        OrderResponse response = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Transitions an order to a new status.
     * This single endpoint handles PAID, SHIPPED, DELIVERED, and CANCELLED transitions.
     * Returns 200 OK with the updated order, or 400/404/409 on error.
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> transitionStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody StatusTransitionRequest request) {
        OrderResponse response = orderService.transitionStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an order by its UUID.
     * Returns 204 No Content, or 404 Not Found.
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
