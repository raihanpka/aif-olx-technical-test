package com.aif.orderservice.order.application.dto;

import java.util.List;

/**
 * Response DTO for paginated order list endpoint.
 */
public class ListOrderResponse {

    private List<OrderResponse> orders;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public ListOrderResponse() {
    }

    public ListOrderResponse(List<OrderResponse> orders, int page, int size,
                             long totalElements, int totalPages) {
        this.orders = orders;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<OrderResponse> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderResponse> orders) {
        this.orders = orders;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
