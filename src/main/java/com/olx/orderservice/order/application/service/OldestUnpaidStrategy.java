package com.olx.orderservice.order.application.service;

import com.olx.orderservice.order.domain.model.Order;
import com.olx.orderservice.order.domain.model.OrderStatus;

import java.util.Comparator;

/**
 * Sorting strategy that prioritizes unpaid orders (CREATED status) by creation date ascending.
 * Oldest unpaid orders appear first, followed by all other orders.
 */
public class OldestUnpaidStrategy implements OrderSortStrategy {

    @Override
    public Comparator<Order> getComparator() {
        return Comparator
            .<Order, Boolean>comparing(
                order -> order.getStatus() == OrderStatus.CREATED,
                Comparator.reverseOrder()
            )
            .thenComparing(Order::getCreatedAt);
    }

    @Override
    public String getKey() {
        return "oldest_unpaid";
    }
}
