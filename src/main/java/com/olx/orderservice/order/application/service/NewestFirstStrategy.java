package com.olx.orderservice.order.application.service;

import com.olx.orderservice.order.domain.model.Order;

import java.util.Comparator;

/**
 * Sorting strategy that orders orders by creation date descending.
 * Newest orders appear first.
 */
public class NewestFirstStrategy implements OrderSortStrategy {

    @Override
    public Comparator<Order> getComparator() {
        return Comparator.comparing(Order::getCreatedAt).reversed();
    }

    @Override
    public String getKey() {
        return "newest";
    }
}
