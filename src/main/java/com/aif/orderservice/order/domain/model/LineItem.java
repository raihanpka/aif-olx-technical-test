package com.aif.orderservice.order.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A value object representing a single line item within an order.
 * Contains the product name, quantity, and unit price.
 * Immutable once created.
 */
public record LineItem(
    String productName,
    int quantity,
    BigDecimal unitPrice
) {
    /**
     * Canonical constructor with validation.
     *
     * @param productName the name of the product, must not be blank
     * @param quantity    the quantity ordered, must be positive
     * @param unitPrice   the price per unit, must be non negative and have scale 2
     */
    public LineItem {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non negative");
        }
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the total price for this line item: quantity * unitPrice.
     */
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
