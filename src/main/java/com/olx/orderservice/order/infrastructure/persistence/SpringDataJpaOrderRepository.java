package com.olx.orderservice.order.infrastructure.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository interface.
 * Internal to the persistence layer and not exposed to the domain.
 * JpaOrderRepository wraps this to provide domain level access.
 */
interface SpringDataJpaOrderRepository extends CrudRepository<OrderEntity, UUID> {
}
