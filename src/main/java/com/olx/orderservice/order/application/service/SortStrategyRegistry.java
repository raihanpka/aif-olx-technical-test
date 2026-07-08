package com.olx.orderservice.order.application.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of available sort strategies.
 * New strategies can be added by creating a new OrderSortStrategy implementation
 * and registering it here. No existing code needs to be modified.
 */
@Component
public class SortStrategyRegistry {

    private final Map<String, OrderSortStrategy> strategies = new HashMap<>();

    @PostConstruct
    public void init() {
        register(new NewestFirstStrategy());
        register(new HighestTotalStrategy());
        register(new OldestUnpaidStrategy());
    }

    /**
     * Registers a strategy by its key.
     */
    public void register(OrderSortStrategy strategy) {
        strategies.put(strategy.getKey(), strategy);
    }

    /**
     * Returns the strategy for the given key.
     * Falls back to NewestFirstStrategy if the key is not recognized.
     */
    public OrderSortStrategy getStrategy(String key) {
        return strategies.getOrDefault(key, new NewestFirstStrategy());
    }

    /**
     * Returns all registered strategy keys.
     */
    public List<String> getAvailableKeys() {
        return List.copyOf(strategies.keySet());
    }
}
