package com.aif.orderservice.order.application.exception;

/**
 * Thrown when an attempt is made to modify order items after payment.
 */
public class OrderItemModificationException extends RuntimeException {

    public OrderItemModificationException(String message) {
        super(message);
    }
}
