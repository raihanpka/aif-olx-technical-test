package com.olx.orderservice.order.domain.service;

import com.olx.orderservice.order.domain.model.Order;
import com.olx.orderservice.order.domain.model.OrderStatus;
import org.springframework.stereotype.Service;

/**
 * Domain service for complex order operations that span multiple aggregates
 * or require coordination with external domain concepts.
 * Currently serves as a thin wrapper for order validation
 * but designed to accommodate future domain complexity.
 */
@Service
public class OrderDomainService {

    /**
     * Validates and executes a status transition on the given order.
     *
     * @param order  the order to transition
     * @param target the target status
     * @param reason optional reason required for CANCELLED transitions
     * @throws IllegalStateException    if the transition is not allowed
     * @throws IllegalArgumentException if a reason is required but missing
     */
    public void transitionStatus(Order order, OrderStatus target, String reason) {
        order.transitionStatus(target, reason);
    }

    /**
     * Validates that the order can be updated with the given details.
     */
    public void validateUpdate(Order order) {
        if (order.isItemsLocked()) {
            throw new IllegalStateException("Cannot modify items after payment");
        }
    }
}
