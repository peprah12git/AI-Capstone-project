package com.AI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class OrderCLI {
    private List<OrderWithInventory> orderList = new ArrayList<>();
    private InventoryService inventoryService;
    private OrderRepository orderRepository;
    private Scanner scanner;
    private int nextOrderId = 1;

    public OrderCLI() {
        this.inventoryService = new DatabaseInventoryService();
        this.orderRepository = new OrderRepository();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        // Initialize the database
        DatabaseInitializer.initialize();
        
        // Create and run the CLI
        OrderCLI cli = new OrderCLI();
        cli.run();
    }

    public void run() {
        System.out.println("=".repeat(60));
        System.out.println("          Welcome to Order Management System");
        System.out.println("=".repeat(60));
        System.out.println();

        boolean continueOrdering = true;
        
        while (continueOrdering) {
            try {
                createOrder();
                
                System.out.print("\nWould you like to create another order? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                continueOrdering = response.equals("yes") || response.equals("y");
                System.out.println();
                
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                System.out.print("\nWould you like to try again? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                continueOrdering = response.equals("yes") || response.equals("y");
                System.out.println();
            }
        }

        // Print final summary
        printSummaryReport();
        printRemainingInventory();
        
        scanner.close();
        System.out.println("\nThank you for using Order Management System!");
    }

    private void createOrder() {
        System.out.println("-".repeat(60));
        System.out.println("Creating Order #" + nextOrderId);
        System.out.println("-".repeat(60));

        // Get customer name
        String customerName = getCustomerName();
        
        // Get products
        List<String> products = getProducts();
        
        // Get quantities
        List<Integer> quantities = getQuantities(products.size());

        // Create the order
        OrderWithInventory order = null;
        try {
            order = new OrderWithInventory(nextOrderId, customerName, products, 
                                          quantities, inventoryService);
            
            System.out.println("\nOrder created successfully!");
            System.out.println("Order ID: " + nextOrderId);
            System.out.println("Customer: " + customerName);
            System.out.println("Status: CREATED");
            
            // Automatically confirm the order
            System.out.println("\nAttempting to confirm order...");
            order.confirmOrder();
            
            // Save to database after successful confirmation
            int savedOrderId = orderRepository.saveOrder(order);
            System.out.println("✓ Order confirmed successfully!");
            System.out.println("✓ Order saved to database with ID: " + savedOrderId);
            
            // Add to order list
            orderList.add(order);
            nextOrderId++;
            
        } catch (InsufficientInventoryException e) {
            System.out.println("\n✗ ERROR - Insufficient Inventory:");
            System.out.println("  " + e.getMessage());
            System.out.println("  Order could not be confirmed due to lack of stock.");
            System.out.println("  The order has been discarded.");
            
        } catch (InvalidStatusTransitionException e) {
            System.out.println("\n✗ ERROR - Invalid Status Transition:");
            System.out.println("  " + e.getMessage());
            System.out.println("  The order has been discarded.");
            
        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ ERROR - Invalid Order Data:");
            System.out.println("  " + e.getMessage());
            System.out.println("  The order has been discarded.");
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR - Unexpected Error:");
            System.out.println("  " + e.getMessage());
            System.out.println("  The order has been discarded.");
        }
    }

    private String getCustomerName() {
        while (true) {
            System.out.print("Enter customer name: ");
            String name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                System.out.println("✗ Customer name cannot be empty. Please try again.");
                continue;
            }
            
            if (name.length() > 255) {
                System.out.println("✗ Customer name too long (max 255 characters). Please try again.");
                continue;
            }
            
            return name;
        }
    }

    private List<String> getProducts() {
        while (true) {
            System.out.print("Enter products (comma-separated): ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("✗ Products cannot be empty. Please try again.");
                continue;
            }
            
            List<String> products = new ArrayList<>();
            String[] parts = input.split(",");
            
            boolean hasError = false;
            for (String part : parts) {
                String product = part.trim();
                if (product.isEmpty()) {
                    System.out.println("✗ Product names cannot be empty. Please try again.");
                    hasError = true;
                    break;
                }
                products.add(product);
            }
            
            if (hasError) {
                continue;
            }
            
            if (products.isEmpty()) {
                System.out.println("✗ At least one product is required. Please try again.");
                continue;
            }
            
            return products;
        }
    }

    private List<Integer> getQuantities(int expectedCount) {
        while (true) {
            System.out.print("Enter quantities (comma-separated, " + expectedCount + " values): ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("✗ Quantities cannot be empty. Please try again.");
                continue;
            }
            
            List<Integer> quantities = new ArrayList<>();
            String[] parts = input.split(",");
            
            if (parts.length != expectedCount) {
                System.out.println("✗ Number of quantities (" + parts.length + 
                                 ") must match number of products (" + expectedCount + "). Please try again.");
                continue;
            }
            
            boolean hasError = false;
            for (String part : parts) {
                try {
                    int quantity = Integer.parseInt(part.trim());
                    if (quantity < 1) {
                        System.out.println("✗ Quantities must be at least 1. Please try again.");
                        hasError = true;
                        break;
                    }
                    quantities.add(quantity);
                } catch (NumberFormatException e) {
                    System.out.println("✗ Invalid quantity '" + part.trim() + "'. Please enter valid numbers.");
                    hasError = true;
                    break;
                }
            }
            
            if (hasError) {
                continue;
            }
            
            return quantities;
        }
    }

    private void printSummaryReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   ORDER SUMMARY REPORT");
        System.out.println("=".repeat(60));
        
        if (orderList.isEmpty()) {
            System.out.println("\nNo orders have been successfully confirmed.");
            return;
        }
        
        System.out.println("\nTotal Orders Confirmed: " + orderList.size());
        System.out.println();
        
        for (OrderWithInventory order : orderList) {
            System.out.println("-".repeat(60));
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Customer Name: " + order.getCustomerName());
            System.out.println("Status: " + order.getStatus());
            
            System.out.println("Products: " + String.join(", ", order.getProducts()));
            
            // Format quantities as a comma-separated string
            List<String> quantityStrings = new ArrayList<>();
            for (Integer qty : order.getQuantities()) {
                quantityStrings.add(qty.toString());
            }
            System.out.println("Quantities: " + String.join(", ", quantityStrings));
            System.out.println();
        }
        
        System.out.println("=".repeat(60));
    }

    private void printRemainingInventory() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("              REMAINING INVENTORY FROM DATABASE");
        System.out.println("=".repeat(60));
        
        try {
            Connection conn = DatabaseInitializer.getConnection();
            String sql = "SELECT product, stock FROM inventory ORDER BY product";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                
                System.out.println();
                System.out.printf("%-20s %10s%n", "Product", "Stock");
                System.out.println("-".repeat(32));
                
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String product = rs.getString("product");
                    int stock = rs.getInt("stock");
                    System.out.printf("%-20s %10d%n", product, stock);
                }
                
                if (!hasResults) {
                    System.out.println("No inventory data available.");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("\n✗ Error retrieving inventory: " + e.getMessage());
        }
        
        System.out.println("=".repeat(60));
    }

    // Getters for testing purposes
    public List<OrderWithInventory> getOrderList() {
        return orderList;
    }
}
