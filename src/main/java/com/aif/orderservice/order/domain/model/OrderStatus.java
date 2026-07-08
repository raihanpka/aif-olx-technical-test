package com.aif.orderservice.order.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle status of an order.
 * Follows a state machine with defined valid transitions:
 * CREATED -> PAID -> SHIPPED -> DELIVERED
 * CREATED, PAID, SHIPPED can also transition to CANCELLED.
 * DELIVERED and CANCELLED are terminal states.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Set<OrderStatus> TERMINAL_STATES = EnumSet.of(DELIVERED, CANCELLED);

    /**
     * Validates whether a transition from this status to the target status is allowed.
     *
     * @param target the desired next status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PAID || target == CANCELLED;
            case PAID -> target == SHIPPED || target == CANCELLED;
            case SHIPPED -> target == DELIVERED || target == CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    /**
     * Returns true if this is a terminal state where no further transitions are allowed.
     */
    public boolean isTerminal() {
        return TERMINAL_STATES.contains(this);
    }
}
