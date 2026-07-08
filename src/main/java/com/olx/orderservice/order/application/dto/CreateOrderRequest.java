package com.olx.orderservice.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new order.
 * Client supplies customer name and items.
 * orderId, status, totalAmount, createdAt, updatedAt are server managed.
 */
public class CreateOrderRequest {

    @NotBlank(message = "Customer name must not be blank")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @NotEmpty(message = "Order must have at least one line item")
    @Valid
    private List<CreateLineItem> items;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String customerName, List<CreateLineItem> items) {
        this.customerName = customerName;
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<CreateLineItem> getItems() {
        return items;
    }

    public void setItems(List<CreateLineItem> items) {
        this.items = items;
    }

    /**
     * Nested DTO for line item creation.
     */
    public static class CreateLineItem {

        @NotBlank(message = "Product name must not be blank")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        private String productName;

        @Positive(message = "Quantity must be at least 1")
        private int quantity;

        @jakarta.validation.constraints.DecimalMin(
            value = "0.00", inclusive = true,
            message = "Unit price must be non negative"
        )
        private BigDecimal unitPrice;

        public CreateLineItem() {
        }

        public CreateLineItem(String productName, int quantity, BigDecimal unitPrice) {
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
