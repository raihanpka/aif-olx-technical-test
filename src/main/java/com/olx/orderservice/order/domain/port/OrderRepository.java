package com.olx.orderservice.order.domain.port;

import com.olx.orderservice.order.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port interface for Order aggregate.
 * Defined in the domain layer as a pure interface with no framework dependencies.
 * Implementation is provided by the infrastructure layer.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findAll();

    void deleteById(UUID orderId);

    boolean existsById(UUID orderId);

    long count();
}
