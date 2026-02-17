package com.AI;

import java.util.Arrays;

public class OrderManagementDemo {
    
    public static void main(String[] args) {
        // Initialize database
        DatabaseInitializer.initialize();
        
        DatabaseInventoryService inventoryService = new DatabaseInventoryService();
        OrderRepository orderRepository = new OrderRepository();
        
        System.out.println("=== Initial Inventory ===");
        System.out.println("Book: " + inventoryService.getStock("Book"));
        System.out.println("Pen: " + inventoryService.getStock("Pen"));
        System.out.println("Notebook: " + inventoryService.getStock("Notebook"));
        
        // Create and process order
        System.out.println("\n=== Creating Order ===");
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Book", "Pen"), Arrays.asList(3, 10), inventoryService);
        int orderId = orderRepository.saveOrder(order);
        System.out.println("Order created with ID: " + orderId);
        
        // Confirm order
        System.out.println("\n=== Confirming Order ===");
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        System.out.println("Order confirmed. Status: " + order.getStatus());
        System.out.println("Book stock: " + inventoryService.getStock("Book"));
        System.out.println("Pen stock: " + inventoryService.getStock("Pen"));
        
        // Deliver order
        System.out.println("\n=== Delivering Order ===");
        order.deliverOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        System.out.println("Order delivered. Status: " + order.getStatus());
        
        // Try to create order with insufficient stock
        System.out.println("\n=== Testing Insufficient Stock ===");
        try {
            OrderWithInventory order2 = new OrderWithInventory(2, "Jane Smith", 
                Arrays.asList("Book"), Arrays.asList(20), inventoryService);
            orderRepository.saveOrder(order2);
            order2.confirmOrder();
        } catch (InsufficientInventoryException e) {
            System.out.println("Order failed: " + e.getMessage());
        }
        
        System.out.println("\n=== Final Inventory ===");
        System.out.println("Book: " + inventoryService.getStock("Book"));
        System.out.println("Pen: " + inventoryService.getStock("Pen"));
        System.out.println("Notebook: " + inventoryService.getStock("Notebook"));
    }
}
