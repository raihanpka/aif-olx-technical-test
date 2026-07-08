package com.aif.orderservice.order.domain;

import com.aif.orderservice.order.domain.model.LineItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for LineItem value object validation and computation.
 */
class LineItemTest {

    @Test
    void shouldCreateValidLineItem() {
        LineItem item = new LineItem("Apple", 3, new BigDecimal("0.50"));
        assertEquals("Apple", item.productName());
        assertEquals(3, item.quantity());
        assertEquals(0, new BigDecimal("0.50").compareTo(item.unitPrice()));
    }

    @Test
    void shouldRejectBlankProductName() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("", 1, new BigDecimal("1.00")));
    }

    @Test
    void shouldRejectNullProductName() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem(null, 1, new BigDecimal("1.00")));
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Apple", 0, new BigDecimal("1.00")));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Apple", -1, new BigDecimal("1.00")));
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Apple", 1, new BigDecimal("-0.50")));
    }

    @Test
    void shouldRejectNullUnitPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Apple", 1, null));
    }

    @Test
    void shouldAcceptZeroUnitPrice() {
        LineItem item = new LineItem("Apple", 1, BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(item.unitPrice()));
    }

    @Test
    void shouldCalculateTotalPrice() {
        LineItem item = new LineItem("Apple", 3, new BigDecimal("0.50"));
        assertEquals(0, new BigDecimal("1.50").compareTo(item.getTotalPrice()));
    }

    @Test
    void shouldScaleUnitPrice() {
        LineItem item = new LineItem("Apple", 1, new BigDecimal("1.555"));
        assertEquals(2, item.unitPrice().scale());
        assertEquals(0, new BigDecimal("1.56").compareTo(item.unitPrice()));
    }
}
