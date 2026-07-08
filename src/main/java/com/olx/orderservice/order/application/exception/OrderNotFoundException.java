package com.olx.orderservice.order.application.exception;

import java.util.UUID;

/**
 * Thrown when an order with a given ID is not found.
 */
public class OrderNotFoundException extends RuntimeException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("Order not found with id: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
