package com.aif.orderservice.order.application;

import com.aif.orderservice.order.application.dto.CreateOrderRequest;
import com.aif.orderservice.order.application.dto.ListOrderResponse;
import com.aif.orderservice.order.application.dto.OrderResponse;
import com.aif.orderservice.order.application.dto.StatusTransitionRequest;
import com.aif.orderservice.order.application.dto.UpdateOrderRequest;
import com.aif.orderservice.order.application.exception.IllegalStatusTransitionException;
import com.aif.orderservice.order.application.exception.OrderItemModificationException;
import com.aif.orderservice.order.application.exception.OrderNotFoundException;
import com.aif.orderservice.order.application.service.OrderApplicationService;
import com.aif.orderservice.order.domain.model.LineItem;
import com.aif.orderservice.order.domain.model.Order;
import com.aif.orderservice.order.domain.model.OrderStatus;
import com.aif.orderservice.order.domain.port.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OrderApplicationService use cases.
 * Uses Mockito to mock the repository layer.
 */
@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(orderRepository);
    }

    @Test
    void shouldCreateOrder() {
        CreateOrderRequest request = new CreateOrderRequest("Andi Wijaya", List.of(
            new CreateOrderRequest.CreateLineItem("Apple", 3, new BigDecimal("0.50")),
            new CreateOrderRequest.CreateLineItem("Bread Loaf", 1, new BigDecimal("2.20"))
        ));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.createOrder(request);

        assertNotNull(response.getOrderId());
        assertEquals("Andi Wijaya", response.getCustomerName());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(0, new BigDecimal("3.70").compareTo(response.getTotalAmount()));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldGetOrderById() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, "Andi", List.of(
            new LineItem("Apple", 3, new BigDecimal("0.50"))
        ), OrderStatus.CREATED, new BigDecimal("1.50"), null, null, null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = service.getOrder(orderId);

        assertEquals(orderId, response.getOrderId());
        assertEquals("Andi", response.getCustomerName());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.getOrder(orderId));
    }

    @Test
    void shouldListOrdersWithPagination() {
        Order order1 = Order.reconstitute(UUID.randomUUID(), "Andi", List.of(
            new LineItem("Apple", 1, new BigDecimal("1.00"))
        ), OrderStatus.CREATED, new BigDecimal("1.00"), null, null, null);

        when(orderRepository.findAll()).thenReturn(List.of(order1));

        ListOrderResponse response = service.listOrders(0, 20, "newest");

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getOrders().size());
    }

    @Test
    void shouldUpdateOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, "Andi", List.of(
            new LineItem("Apple", 3, new BigDecimal("0.50"))
        ), OrderStatus.CREATED, new BigDecimal("1.50"), null, null, null);

        UpdateOrderRequest request = new UpdateOrderRequest("Budi", List.of(
            new CreateOrderRequest.CreateLineItem("Orange", 2, new BigDecimal("1.00"))
        ));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.updateOrder(orderId, request);

        assertEquals("Budi", response.getCustomerName());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void shouldRejectItemUpdateWhenPaid() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, "Andi", List.of(
            new LineItem("Apple", 3, new BigDecimal("0.50"))
        ), OrderStatus.PAID, new BigDecimal("1.50"), null, null, null);

        UpdateOrderRequest request = new UpdateOrderRequest("Budi", List.of(
            new CreateOrderRequest.CreateLineItem("Orange", 2, new BigDecimal("1.00"))
        ));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderItemModificationException.class,
            () -> service.updateOrder(orderId, request));
    }

    @Test
    void shouldTransitionStatus() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, "Andi", List.of(
            new LineItem("Apple", 3, new BigDecimal("0.50"))
        ), OrderStatus.CREATED, new BigDecimal("1.50"), null, null, null);

        StatusTransitionRequest request = new StatusTransitionRequest(OrderStatus.PAID, null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.transitionStatus(orderId, request);

        assertEquals(OrderStatus.PAID, response.getStatus());
    }

    @Test
    void shouldRejectIllegalStatusTransition() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, "Andi", List.of(
            new LineItem("Apple", 3, new BigDecimal("0.50"))
        ), OrderStatus.CREATED, new BigDecimal("1.50"), null, null, null);

        StatusTransitionRequest request = new StatusTransitionRequest(OrderStatus.SHIPPED, null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStatusTransitionException.class,
            () -> service.transitionStatus(orderId, request));
    }

    @Test
    void shouldDeleteOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.existsById(orderId)).thenReturn(true);

        service.deleteOrder(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder(orderId));
    }
}
