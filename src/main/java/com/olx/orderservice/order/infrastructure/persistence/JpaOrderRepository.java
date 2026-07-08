package com.olx.orderservice.order.infrastructure.persistence;

import com.olx.orderservice.order.domain.model.Order;
import com.olx.orderservice.order.domain.port.OrderRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * JPA implementation of OrderRepository port.
 * Bridges between the domain layer port interface and Spring Data JPA.
 */
@Repository
@Primary
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataJpaOrderRepository springRepository;

    public JpaOrderRepository(SpringDataJpaOrderRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderPersistenceMapper.toEntity(order);
        OrderEntity saved = springRepository.save(entity);
        return OrderPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return springRepository.findById(orderId)
            .map(OrderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return StreamSupport.stream(springRepository.findAll().spliterator(), false)
            .map(OrderPersistenceMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID orderId) {
        springRepository.deleteById(orderId);
    }

    @Override
    public boolean existsById(UUID orderId) {
        return springRepository.existsById(orderId);
    }

    @Override
    public long count() {
        return springRepository.count();
    }
}
