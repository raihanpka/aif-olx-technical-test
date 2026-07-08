package com.aif.orderservice.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for updating an existing order.
 * Only customerName and items can be updated.
 * Status changes are handled via a separate endpoint.
 */
public class UpdateOrderRequest {

    @NotBlank(message = "Customer name must not be blank")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @NotEmpty(message = "Order must have at least one line item")
    @Valid
    private List<CreateOrderRequest.CreateLineItem> items;

    public UpdateOrderRequest() {
    }

    public UpdateOrderRequest(String customerName, List<CreateOrderRequest.CreateLineItem> items) {
        this.customerName = customerName;
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<CreateOrderRequest.CreateLineItem> getItems() {
        return items;
    }

    public void setItems(List<CreateOrderRequest.CreateLineItem> items) {
        this.items = items;
    }
}
