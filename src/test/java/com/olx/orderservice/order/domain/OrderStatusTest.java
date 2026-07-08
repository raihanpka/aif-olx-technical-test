package com.olx.orderservice.order.domain;

import com.olx.orderservice.order.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for OrderStatus enum transition validation.
 */
class OrderStatusTest {

    @Test
    void shouldAllowFromCreatedToPaid() {
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.PAID));
    }

    @Test
    void shouldAllowFromCreatedToCancelled() {
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void shouldNotAllowFromCreatedToShipped() {
        assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.SHIPPED));
    }

    @Test
    void shouldNotAllowFromCreatedToDelivered() {
        assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void shouldAllowFromPaidToShipped() {
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED));
    }

    @Test
    void shouldAllowFromPaidToCancelled() {
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void shouldNotAllowFromPaidToCreated() {
        assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.CREATED));
    }

    @Test
    void shouldNotAllowFromPaidToDelivered() {
        assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void shouldAllowFromShippedToDelivered() {
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void shouldAllowFromShippedToCancelled() {
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void shouldNotAllowFromShippedToPaid() {
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PAID));
    }

    @Test
    void shouldNotAllowFromDeliveredTransitions() {
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CREATED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PAID));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void shouldNotAllowFromCancelledTransitions() {
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CREATED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PAID));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void shouldDetectTerminalStates() {
        assertTrue(OrderStatus.DELIVERED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertFalse(OrderStatus.CREATED.isTerminal());
        assertFalse(OrderStatus.PAID.isTerminal());
        assertFalse(OrderStatus.SHIPPED.isTerminal());
    }
}
