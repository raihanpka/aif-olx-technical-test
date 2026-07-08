package com.olx.orderservice.order.infrastructure.persistence;

import com.olx.orderservice.order.domain.model.LineItem;
import com.olx.orderservice.order.domain.model.Order;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain Order and JPA OrderEntity.
 * Keeps the mapping logic isolated so changes to JPA structure
 * do not affect the domain model.
 */
public class OrderPersistenceMapper {

    private OrderPersistenceMapper() {
    }

    /**
     * Converts a domain Order to a JPA OrderEntity for persistence.
     */
    public static OrderEntity toEntity(Order domain) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(domain.getOrderId());
        entity.setCustomerName(domain.getCustomerName());
        entity.setStatus(domain.getStatus());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setCancellationReason(domain.getCancellationReason());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<OrderLineItemEntity> itemEntities = domain.getLineItems().stream()
            .map(item -> {
                OrderLineItemEntity itemEntity = new OrderLineItemEntity(
                    item.productName(),
                    item.quantity(),
                    item.unitPrice()
                );
                itemEntity.setOrder(entity);
                return itemEntity;
            })
            .collect(Collectors.toList());

        entity.setLineItems(itemEntities);
        return entity;
    }

    /**
     * Converts a JPA OrderEntity back to a domain Order.
     */
    public static Order toDomain(OrderEntity entity) {
        List<LineItem> lineItems = entity.getLineItems().stream()
            .map(item -> new LineItem(
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice()
            ))
            .collect(Collectors.toList());

        return Order.reconstitute(
            entity.getOrderId(),
            entity.getCustomerName(),
            lineItems,
            entity.getStatus(),
            entity.getTotalAmount(),
            entity.getCancellationReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
