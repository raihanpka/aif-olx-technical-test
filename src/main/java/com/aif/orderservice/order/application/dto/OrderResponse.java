package com.aif.orderservice.order.application.dto;

import com.aif.orderservice.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO representing an order returned to the client.
 * Contains all order fields that are safe to expose.
 */
public class OrderResponse {

    private UUID orderId;
    private String customerName;
    private List<LineItemResponse> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String cancellationReason;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderResponse() {
    }

    public OrderResponse(UUID orderId, String customerName, List<LineItemResponse> items,
                         OrderStatus status, BigDecimal totalAmount,
                         String cancellationReason, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = items;
        this.status = status;
        this.totalAmount = totalAmount;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<LineItemResponse> getItems() {
        return items;
    }

    public void setItems(List<LineItemResponse> items) {
        this.items = items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Nested response DTO for a single line item.
     */
    public static class LineItemResponse {

        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public LineItemResponse() {
        }

        public LineItemResponse(String productName, int quantity, BigDecimal unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
