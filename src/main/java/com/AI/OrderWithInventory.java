package com.AI;

import java.util.List;

public class OrderWithInventory {
    private Integer orderId;
    private String customerName;
    private List<String> products;
    private List<Integer> quantities;
    private OrderStatus status;
    private InventoryService inventoryService;

    public OrderWithInventory(Integer orderId, String customerName, List<String> products, 
                              List<Integer> quantities, InventoryService inventoryService) {
        validateOrderId(orderId);
        validateCustomerName(customerName);
        validateProducts(products);
        validateQuantities(quantities);
        validateArrayLengths(products, quantities);
        
        this.orderId = orderId;
        this.customerName = customerName;
        this.products = products;
        this.quantities = quantities;
        this.status = OrderStatus.CREATED;
        this.inventoryService = inventoryService;
    }

    public void confirmOrder() {
        validateTransition(status, OrderStatus.CONFIRMED);
        if (!inventoryService.checkStock(products, quantities)) {
            throw new InsufficientInventoryException("Insufficient inventory to confirm order");
        }
        inventoryService.reserveStock(products, quantities);
        status = OrderStatus.CONFIRMED;
    }

    public void deliverOrder() {
        validateTransition(status, OrderStatus.DELIVERED);
        status = OrderStatus.DELIVERED;
    }

    public void cancelOrder() {
        validateTransition(status, OrderStatus.CANCELLED);
        if (status == OrderStatus.CONFIRMED) {
            inventoryService.returnStock(products, quantities);
        }
        status = OrderStatus.CANCELLED;
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case CREATED:
                if (next != OrderStatus.CONFIRMED && next != OrderStatus.CANCELLED) {
                    throw new InvalidStatusTransitionException("Created can only transition to Confirmed or Cancelled");
                }
                break;
            case CONFIRMED:
                if (next != OrderStatus.DELIVERED && next != OrderStatus.CANCELLED) {
                    throw new InvalidStatusTransitionException("Confirmed can only transition to Delivered or Cancelled");
                }
                break;
            case DELIVERED:
                throw new InvalidStatusTransitionException("Cannot transition from Delivered status");
            case CANCELLED:
                throw new InvalidStatusTransitionException("Cannot transition from Cancelled status");
        }
    }

    private void validateOrderId(Integer orderId) {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("Order ID must be at least 1");
        }
    }

    private void validateCustomerName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
    }

    private void validateProducts(List<String> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products list must contain at least one item");
        }
    }

    private void validateQuantities(List<Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            throw new IllegalArgumentException("Quantities list must contain at least one item");
        }
        for (Integer quantity : quantities) {
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Quantities must be at least 1");
            }
        }
    }

    private void validateArrayLengths(List<String> products, List<Integer> quantities) {
        if (products.size() != quantities.size()) {
            throw new IllegalArgumentException("Products and quantities must have matching lengths");
        }
    }

    public Integer getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public List<String> getProducts() { return products; }
    public List<Integer> getQuantities() { return quantities; }
    public OrderStatus getStatus() { return status; }

    public enum OrderStatus {
        CREATED, CONFIRMED, DELIVERED, CANCELLED
    }
}
