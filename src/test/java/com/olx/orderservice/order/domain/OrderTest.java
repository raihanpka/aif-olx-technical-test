package com.olx.orderservice.order.domain;

import com.olx.orderservice.order.domain.model.LineItem;
import com.olx.orderservice.order.domain.model.Order;
import com.olx.orderservice.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Order aggregate root behavior.
 */
class OrderTest {

    private List<LineItem> items;

    @BeforeEach
    void setUp() {
        items = List.of(
            new LineItem("Apple", 3, new BigDecimal("0.50")),
            new LineItem("Bread Loaf", 1, new BigDecimal("2.20"))
        );
    }

    @Test
    void shouldCreateOrderWithDefaultStatus() {
        Order order = Order.create("Andi Wijaya", items);
        assertNotNull(order.getOrderId());
        assertEquals("Andi Wijaya", order.getCustomerName());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(2, order.getLineItems().size());
    }

    @Test
    void shouldComputeTotalAmount() {
        Order order = Order.create("Andi Wijaya", items);
        // 3 * 0.50 + 1 * 2.20 = 1.50 + 2.20 = 3.70
        assertEquals(0, new BigDecimal("3.70").compareTo(order.getTotalAmount()));
    }

    @Test
    void shouldRejectBlankCustomerName() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.create("", items));
    }

    @Test
    void shouldRejectNullCustomerName() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.create(null, items));
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.create("Andi", List.of()));
    }

    @Test
    void shouldRejectNullItems() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.create("Andi", null));
    }

    @Test
    void shouldTransitionFromCreatedToPaid() {
        Order order = Order.create("Andi", items);
        order.transitionStatus(OrderStatus.PAID, null);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void shouldTransitionFromCreatedToCancelledWithReason() {
        Order order = Order.create("Andi", items);
        order.transitionStatus(OrderStatus.CANCELLED, "Changed mind");
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals("Changed mind", order.getCancellationReason());
    }

    @Test
    void shouldRejectCancellationWithoutReason() {
        Order order = Order.create("Andi", items);
        assertThrows(IllegalArgumentException.class,
            () -> order.transitionStatus(OrderStatus.CANCELLED, null));
    }

    @Test
    void shouldRejectCancellationWithBlankReason() {
        Order order = Order.create("Andi", items);
        assertThrows(IllegalArgumentException.class,
            () -> order.transitionStatus(OrderStatus.CANCELLED, ""));
    }

    @Test
    void shouldRejectIllegalTransition() {
        Order order = Order.create("Andi", items);
        assertThrows(IllegalStateException.class,
            () -> order.transitionStatus(OrderStatus.SHIPPED, null));
    }

    @Test
    void shouldTransitionFullLifecycle() {
        Order order = Order.create("Andi", items);
        order.transitionStatus(OrderStatus.PAID, null);
        order.transitionStatus(OrderStatus.SHIPPED, null);
        order.transitionStatus(OrderStatus.DELIVERED, null);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void shouldNotTransitionFromTerminalState() {
        Order order = Order.create("Andi", items);
        order.transitionStatus(OrderStatus.CANCELLED, "Reason");
        assertThrows(IllegalStateException.class,
            () -> order.transitionStatus(OrderStatus.PAID, null));
    }

    @Test
    void shouldUpdateDetailsBeforePayment() {
        Order order = Order.create("Andi", items);
        List<LineItem> newItems = List.of(
            new LineItem("Orange", 2, new BigDecimal("1.00"))
        );
        order.updateDetails("Budi", newItems);
        assertEquals("Budi", order.getCustomerName());
        assertEquals(1, order.getLineItems().size());
    }

    @Test
    void shouldRejectItemUpdateAfterPayment() {
        Order order = Order.create("Andi", items);
        order.transitionStatus(OrderStatus.PAID, null);
        List<LineItem> newItems = List.of(
            new LineItem("Orange", 2, new BigDecimal("1.00"))
        );
        assertThrows(IllegalStateException.class,
            () -> order.updateDetails("Budi", newItems));
    }

    @Test
    void shouldRejectUpdateWithBlankName() {
        Order order = Order.create("Andi", items);
        assertThrows(IllegalArgumentException.class,
            () -> order.updateDetails("", items));
    }

    @Test
    void shouldRejectUpdateWithEmptyItems() {
        Order order = Order.create("Andi", items);
        assertThrows(IllegalArgumentException.class,
            () -> order.updateDetails("Budi", List.of()));
    }

    @Test
    void shouldLockItemsAfterPayment() {
        Order order = Order.create("Andi", items);
        assertFalse(order.isItemsLocked());
        order.transitionStatus(OrderStatus.PAID, null);
        assertTrue(order.isItemsLocked());
    }
}
