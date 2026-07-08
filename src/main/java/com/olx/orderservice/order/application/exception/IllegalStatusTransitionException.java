package com.olx.orderservice.order.application.exception;

import com.olx.orderservice.order.domain.model.OrderStatus;

/**
 * Thrown when an illegal status transition is attempted.
 */
public class IllegalStatusTransitionException extends RuntimeException {

    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;

    public IllegalStatusTransitionException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Cannot transition from " + currentStatus + " to " + targetStatus);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
}
