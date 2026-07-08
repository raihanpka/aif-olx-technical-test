package com.olx.orderservice.order.application.service;

import com.olx.orderservice.order.domain.model.Order;

import java.util.Comparator;

/**
 * Sorting strategy that orders orders by total amount descending.
 * Orders with the highest total value appear first.
 */
public class HighestTotalStrategy implements OrderSortStrategy {

    @Override
    public Comparator<Order> getComparator() {
        return Comparator.comparing(Order::getTotalAmount).reversed();
    }

    @Override
    public String getKey() {
        return "highest_total";
    }
}
