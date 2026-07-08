package com.aif.orderservice.order.application.dto;

import com.aif.orderservice.order.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for transitioning an order to a new status.
 * The cancellationReason field is required only when transitioning to CANCELLED.
 */
public class StatusTransitionRequest {

    @NotNull(message = "Target status must not be null")
    private OrderStatus status;

    private String cancellationReason;

    public StatusTransitionRequest() {
    }

    public StatusTransitionRequest(OrderStatus status, String cancellationReason) {
        this.status = status;
        this.cancellationReason = cancellationReason;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
