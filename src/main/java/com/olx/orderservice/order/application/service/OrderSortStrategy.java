package com.olx.orderservice.order.application.service;

import com.olx.orderservice.order.domain.model.Order;

import java.util.Comparator;

/**
 * Strategy interface for order sorting.
 * Each implementation defines a different ordering rule.
 * New sorting rules can be added without modifying existing code.
 */
public interface OrderSortStrategy {

    /**
     * Returns a comparator that implements the sorting rule.
     */
    Comparator<Order> getComparator();

    /**
     * Returns the key used to identify this strategy.
     */
    String getKey();
}
