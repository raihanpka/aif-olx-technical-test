package com.aif.orderservice.order.application.service;

import com.aif.orderservice.order.application.dto.CreateOrderRequest;
import com.aif.orderservice.order.application.dto.OrderResponse;
import com.aif.orderservice.order.domain.model.LineItem;
import com.aif.orderservice.order.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain model and DTO objects.
 * Keeps the mapping logic isolated and testable.
 */
public class OrderMapper {

    private OrderMapper() {
    }

    /**
     * Converts a CreateOrderRequest to a list of domain LineItem objects.
     */
    public static List<LineItem> toDomainLineItems(CreateOrderRequest request) {
        return request.getItems().stream()
            .map(item -> new LineItem(
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO
            ))
            .collect(Collectors.toList());
    }

    /**
     * Converts a list of CreateLineItem from UpdateOrderRequest to domain LineItem objects.
     */
    public static List<LineItem> toDomainLineItemsFromUpdate(
            List<CreateOrderRequest.CreateLineItem> items) {
        return items.stream()
            .map(item -> new LineItem(
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO
            ))
            .collect(Collectors.toList());
    }

    /**
     * Converts a domain Order to a response DTO.
     */
    public static OrderResponse toResponse(Order order) {
        List<OrderResponse.LineItemResponse> itemResponses = order.getLineItems().stream()
            .map(item -> new OrderResponse.LineItemResponse(
                item.productName(),
                item.quantity(),
                item.unitPrice()
            ))
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getOrderId(),
            order.getCustomerName(),
            itemResponses,
            order.getStatus(),
            order.getTotalAmount(),
            order.getCancellationReason(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }

    /**
     * Converts a list of domain Orders to a list of response DTOs.
     */
    public static List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
            .map(OrderMapper::toResponse)
            .collect(Collectors.toList());
    }
}
