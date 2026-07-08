package com.olx.orderservice.order.application.exception;

/**
 * Thrown when an order operation fails validation.
 */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
