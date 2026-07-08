package com.aif.orderservice.order.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The aggregate root of the Order domain.
 * Encapsulates all order related business logic including
 * status transitions, item management, and total computation.
 */
public class Order {

    private final UUID orderId;
    private String customerName;
    private final List<LineItem> lineItems;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String cancellationReason;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(UUID orderId, String customerName, List<LineItem> lineItems,
                  OrderStatus status, BigDecimal totalAmount,
                  String cancellationReason, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.lineItems = new ArrayList<>(lineItems);
        this.status = status;
        this.totalAmount = totalAmount;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method to create a new Order.
     * Validates that customer name and line items are provided.
     * Generates a UUID and sets the initial status to CREATED.
     */
    public static Order create(String customerName, List<LineItem> items) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line item");
        }
        BigDecimal total = items.stream()
            .map(LineItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        Instant now = Instant.now();
        return new Order(
            UUID.randomUUID(),
            customerName,
            new ArrayList<>(items),
            OrderStatus.CREATED,
            total,
            null,
            now,
            now
        );
    }

    /**
     * Factory method to reconstitute an existing Order from persistent storage.
     * Skips validation as the entity is assumed to have been validated when created.
     */
    public static Order reconstitute(UUID orderId, String customerName, List<LineItem> lineItems,
                                     OrderStatus status, BigDecimal totalAmount,
                                     String cancellationReason, Instant createdAt, Instant updatedAt) {
        return new Order(orderId, customerName, lineItems, status, totalAmount,
            cancellationReason, createdAt, updatedAt);
    }

    /**
     * Transitions the order to the target status if the transition is valid.
     * CANCELLED status requires a cancellation reason.
     */
    public void transitionStatus(OrderStatus target, String reason) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException(
                "Cannot transition from " + status + " to " + target
            );
        }
        if (target == OrderStatus.CANCELLED && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Cancellation reason must be provided");
        }
        this.status = target;
        if (target == OrderStatus.CANCELLED) {
            this.cancellationReason = reason;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Updates customer name and line items.
     * Items cannot be modified if the order status is PAID or beyond.
     */
    public void updateDetails(String newCustomerName, List<LineItem> newItems) {
        if (newCustomerName == null || newCustomerName.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
        if (newItems == null || newItems.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line item");
        }
        if (isItemsLocked()) {
            throw new IllegalStateException("Cannot modify items after payment");
        }
        this.customerName = newCustomerName;
        this.lineItems.clear();
        this.lineItems.addAll(newItems);
        this.totalAmount = newItems.stream()
            .map(LineItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        this.updatedAt = Instant.now();
    }

    /**
     * Returns true if the line items are locked from modification.
     * Items become locked when the order reaches PAID status or beyond.
     */
    public boolean isItemsLocked() {
        return status.ordinal() >= OrderStatus.PAID.ordinal();
    }

    // Getters

    public UUID getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
