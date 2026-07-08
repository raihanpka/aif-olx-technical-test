package com.olx.orderservice.order.application.service;

import com.olx.orderservice.order.application.dto.CreateOrderRequest;
import com.olx.orderservice.order.application.dto.ListOrderResponse;
import com.olx.orderservice.order.application.dto.OrderResponse;
import com.olx.orderservice.order.application.dto.StatusTransitionRequest;
import com.olx.orderservice.order.application.dto.UpdateOrderRequest;
import com.olx.orderservice.order.application.exception.IllegalStatusTransitionException;
import com.olx.orderservice.order.application.exception.InvalidOrderException;
import com.olx.orderservice.order.application.exception.OrderItemModificationException;
import com.olx.orderservice.order.application.exception.OrderNotFoundException;
import com.olx.orderservice.order.domain.model.LineItem;
import com.olx.orderservice.order.domain.model.Order;
import com.olx.orderservice.order.domain.port.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service orchestrating order use cases.
 * Each method represents a single business operation.
 */
@Service
@Transactional
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final SortStrategyRegistry sortStrategyRegistry;

    public OrderApplicationService(OrderRepository orderRepository,
                                   SortStrategyRegistry sortStrategyRegistry) {
        this.orderRepository = orderRepository;
        this.sortStrategyRegistry = sortStrategyRegistry;
    }

    /**
     * Creates a new order from the request data.
     * Returns the created order as a response DTO.
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<LineItem> lineItems = OrderMapper.toDomainLineItems(request);
        Order order = Order.create(request.getCustomerName(), lineItems);
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @throws OrderNotFoundException if the order does not exist
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        return OrderMapper.toResponse(order);
    }

    /**
     * Retrieves a paginated and sorted list of orders.
     * Uses the SortStrategyRegistry to resolve the sorting strategy by key.
     * Adding a new sorting rule requires only creating a new OrderSortStrategy
     * implementation and registering it. No existing code is modified.
     */
    @Transactional(readOnly = true)
    public ListOrderResponse listOrders(int page, int size, String sort) {
        List<Order> allOrders = orderRepository.findAll();

        String sortKey = (sort != null) ? sort.toLowerCase() : "newest";
        OrderSortStrategy strategy = sortStrategyRegistry.getStrategy(sortKey);

        // Apply sorting strategy
        List<Order> sorted = allOrders.stream()
            .sorted(strategy.getComparator())
            .collect(Collectors.toList());

        // Apply pagination
        int totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Order> pageContent;
        if (fromIndex >= totalElements) {
            pageContent = List.of();
        } else {
            pageContent = sorted.subList(fromIndex, toIndex);
        }

        List<OrderResponse> orderResponses = OrderMapper.toResponseList(pageContent);
        return new ListOrderResponse(orderResponses, page, size, totalElements, totalPages);
    }

    /**
     * Updates an existing order's customer name and items.
     * Items cannot be modified if the order status is PAID or beyond.
     */
    public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request) {
        Order order = findOrderOrThrow(orderId);
        List<LineItem> newItems = OrderMapper.toDomainLineItemsFromUpdate(request.getItems());
        try {
            order.updateDetails(request.getCustomerName(), newItems);
        } catch (IllegalStateException e) {
            throw new OrderItemModificationException(e.getMessage());
        }
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    /**
     * Transitions an order to a new status.
     * Cancellation requires a non blank cancellationReason.
     */
    public OrderResponse transitionStatus(UUID orderId, StatusTransitionRequest request) {
        Order order = findOrderOrThrow(orderId);
        try {
            order.transitionStatus(request.getStatus(), request.getCancellationReason());
        } catch (IllegalStateException e) {
            throw new IllegalStatusTransitionException(order.getStatus(), request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderException(e.getMessage());
        }
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    /**
     * Deletes an order by its ID.
     *
     * @throws OrderNotFoundException if the order does not exist
     */
    public void deleteOrder(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }
        orderRepository.deleteById(orderId);
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

}
