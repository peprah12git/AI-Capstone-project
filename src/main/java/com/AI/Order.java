package com.AI;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Integer orderId;
    private String customerName;
    private List<String> productList;
    private List<Integer> quantityList;
    private OrderStatus status;
    private OrderStatus previousStatus;
    private Metadata metadata;

    public Order(Integer orderId, String customerName, List<String> productList, 
                 List<Integer> quantityList, OrderStatus status) {
        validateOrderId(orderId);
        validateCustomerName(customerName);
        validateProductList(productList);
        validateQuantityList(quantityList);
        validateArrayLengths(productList, quantityList);
        
        this.orderId = orderId;
        this.customerName = customerName;
        this.productList = productList;
        this.quantityList = quantityList;
        this.status = status;
        this.metadata = new Metadata();
        this.metadata.createdAt = LocalDateTime.now();
    }

    public void confirmOrder() {
        updateStatus(OrderStatus.CONFIRMED);
    }

    public void deliverOrder() {
        updateStatus(OrderStatus.DELIVERED);
    }

    public void cancelOrder() {
        updateStatus(OrderStatus.CANCELLED);
    }

    public void updateStatus(OrderStatus newStatus) {
        validateStatusTransition(this.status, newStatus);
        this.previousStatus = this.status;
        this.status = newStatus;
        updateMetadata(newStatus);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        OrderStatusValidator.validateTransition(current, next);
    }

    private void updateMetadata(OrderStatus status) {
        switch (status) {
            case CONFIRMED:
                metadata.confirmedAt = LocalDateTime.now();
                break;
            case DELIVERED:
                metadata.deliveredAt = LocalDateTime.now();
                break;
            case CANCELLED:
                metadata.cancelledAt = LocalDateTime.now();
                break;
        }
    }

    private void validateOrderId(Integer orderId) {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("Order ID must be at least 1");
        }
    }

    private void validateCustomerName(String name) {
        if (name == null || name.isEmpty() || name.length() > 255) {
            throw new IllegalArgumentException("Customer name must be between 1 and 255 characters");
        }
    }

    private void validateProductList(List<String> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Product list must contain at least one item");
        }
        for (String product : products) {
            if (product == null || product.isEmpty()) {
                throw new IllegalArgumentException("Product names cannot be empty");
            }
        }
    }

    private void validateQuantityList(List<Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            throw new IllegalArgumentException("Quantity list must contain at least one item");
        }
        for (Integer quantity : quantities) {
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Quantities must be at least 1");
            }
        }
    }

    private void validateArrayLengths(List<String> products, List<Integer> quantities) {
        if (products.size() != quantities.size()) {
            throw new IllegalArgumentException("Product list and quantity list must have matching lengths");
        }
    }

    // Getters
    public Integer getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public List<String> getProductList() { return productList; }
    public List<Integer> getQuantityList() { return quantityList; }
    public OrderStatus getStatus() { return status; }
    public OrderStatus getPreviousStatus() { return previousStatus; }
    public Metadata getMetadata() { return metadata; }

    public enum OrderStatus {
        CREATED, CONFIRMED, DELIVERED, CANCELLED
    }

    public static class Metadata {
        private LocalDateTime createdAt;
        private LocalDateTime confirmedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime cancelledAt;

        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getConfirmedAt() { return confirmedAt; }
        public LocalDateTime getDeliveredAt() { return deliveredAt; }
        public LocalDateTime getCancelledAt() { return cancelledAt; }
    }
}
