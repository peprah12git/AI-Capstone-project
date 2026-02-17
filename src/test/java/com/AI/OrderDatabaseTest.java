package com.AI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderDatabaseTest {
    private DatabaseInventoryService inventoryService;
    private OrderRepository orderRepository;

    @BeforeAll
    void setup() {
        DatabaseInitializer.setDatabaseUrl("jdbc:sqlite::memory:");
        DatabaseInitializer.initialize();
        inventoryService = new DatabaseInventoryService();
        orderRepository = new OrderRepository();
    }

    @BeforeEach
    void resetInventory() throws Exception {
        Connection conn = DatabaseInitializer.getConnection();
        java.sql.PreparedStatement pstmt = conn.prepareStatement("UPDATE inventory SET stock = ? WHERE product = ?");
        pstmt.setInt(1, 10);
        pstmt.setString(2, "Book");
        pstmt.executeUpdate();
        pstmt.setInt(1, 50);
        pstmt.setString(2, "Pen");
        pstmt.executeUpdate();
        pstmt.setInt(1, 20);
        pstmt.setString(2, "Notebook");
        pstmt.executeUpdate();
        pstmt.close();
    }

    @AfterAll
    void tearDown() throws SQLException {
        DatabaseInitializer.closeSharedConnection();
    }

    @Test
    @DisplayName("Test valid transition: Created → Confirmed → Delivered")
    void testValidTransitionCreatedToConfirmedToDelivered() {
        OrderWithInventory order = new OrderWithInventory(1, "Alice", 
            Arrays.asList("Book"), Arrays.asList(3), inventoryService);
        int orderId = orderRepository.saveOrder(order);
        
        assertEquals(OrderWithInventory.OrderStatus.CREATED, order.getStatus());
        assertEquals(10, inventoryService.getStock("Book"));
        
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(OrderWithInventory.OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(7, inventoryService.getStock("Book"));
        
        order.deliverOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(OrderWithInventory.OrderStatus.DELIVERED, order.getStatus());
        assertEquals(7, inventoryService.getStock("Book"));
    }

    @Test
    @DisplayName("Test invalid transition: Delivered → Cancelled")
    void testInvalidTransitionDeliveredToCancelled() {
        OrderWithInventory order = new OrderWithInventory(1, "Bob", 
            Arrays.asList("Pen"), Arrays.asList(5), inventoryService);
        int orderId = orderRepository.saveOrder(order);
        
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        
        order.deliverOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        
        InvalidStatusTransitionException exception = assertThrows(
            InvalidStatusTransitionException.class, 
            order::cancelOrder
        );
        assertTrue(exception.getMessage().contains("Cannot transition from Delivered"));
        assertEquals(OrderWithInventory.OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    @DisplayName("Test inventory check: cannot confirm if stock insufficient")
    void testCannotConfirmWithInsufficientStock() {
        OrderWithInventory order = new OrderWithInventory(1, "Charlie", 
            Arrays.asList("Book"), Arrays.asList(15), inventoryService);
        int orderId = orderRepository.saveOrder(order);
        
        InsufficientInventoryException exception = assertThrows(
            InsufficientInventoryException.class, 
            order::confirmOrder
        );
        assertTrue(exception.getMessage().contains("Insufficient inventory"));
        assertEquals(OrderWithInventory.OrderStatus.CREATED, order.getStatus());
        assertEquals(10, inventoryService.getStock("Book"));
    }

    @Test
    @DisplayName("Test cancellation restores stock if order was confirmed")
    void testCancellationRestoresStockFromConfirmed() {
        OrderWithInventory order = new OrderWithInventory(1, "Diana", 
            Arrays.asList("Notebook"), Arrays.asList(8), inventoryService);
        int orderId = orderRepository.saveOrder(order);
        
        assertEquals(20, inventoryService.getStock("Notebook"));
        
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(12, inventoryService.getStock("Notebook"));
        
        order.cancelOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(OrderWithInventory.OrderStatus.CANCELLED, order.getStatus());
        assertEquals(20, inventoryService.getStock("Notebook"));
    }

    @Test
    @DisplayName("Test cancellation from Created does not restore stock")
    void testCancellationFromCreatedDoesNotRestoreStock() {
        OrderWithInventory order = new OrderWithInventory(1, "Eve", 
            Arrays.asList("Pen"), Arrays.asList(10), inventoryService);
        int orderId = orderRepository.saveOrder(order);
        
        assertEquals(50, inventoryService.getStock("Pen"));
        
        order.cancelOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(OrderWithInventory.OrderStatus.CANCELLED, order.getStatus());
        assertEquals(50, inventoryService.getStock("Pen"));
    }

    @Test
    @DisplayName("Test multiple products inventory check")
    void testMultipleProductsInventoryCheck() {
        List<String> products = Arrays.asList("Book", "Pen", "Notebook");
        List<Integer> quantities = Arrays.asList(5, 20, 10);
        
        OrderWithInventory order = new OrderWithInventory(1, "Frank", 
            products, quantities, inventoryService);
        int orderId = orderRepository.saveOrder(order);
        
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        
        assertEquals(5, inventoryService.getStock("Book"));
        assertEquals(30, inventoryService.getStock("Pen"));
        assertEquals(10, inventoryService.getStock("Notebook"));
    }

    @Test
    @DisplayName("Test insufficient stock for one product in multi-product order")
    void testInsufficientStockForOneProductInMultiProductOrder() {
        List<String> products = Arrays.asList("Book", "Pen");
        List<Integer> quantities = Arrays.asList(15, 10);
        
        OrderWithInventory order = new OrderWithInventory(1, "Grace", 
            products, quantities, inventoryService);
        
        assertThrows(InsufficientInventoryException.class, order::confirmOrder);
        assertEquals(10, inventoryService.getStock("Book"));
        assertEquals(50, inventoryService.getStock("Pen"));
    }

    @Test
    @DisplayName("Test invalid transition: Created → Delivered")
    void testInvalidTransitionCreatedToDelivered() {
        OrderWithInventory order = new OrderWithInventory(1, "Henry", 
            Arrays.asList("Book"), Arrays.asList(2), inventoryService);
        
        InvalidStatusTransitionException exception = assertThrows(
            InvalidStatusTransitionException.class, 
            order::deliverOrder
        );
        assertTrue(exception.getMessage().contains("Created can only transition"));
    }

    @Test
    @DisplayName("Test invalid transition: Cancelled → Confirmed")
    void testInvalidTransitionCancelledToConfirmed() {
        OrderWithInventory order = new OrderWithInventory(1, "Ivy", 
            Arrays.asList("Pen"), Arrays.asList(5), inventoryService);
        
        order.cancelOrder();
        assertEquals(OrderWithInventory.OrderStatus.CANCELLED, order.getStatus());
        
        InvalidStatusTransitionException exception = assertThrows(
            InvalidStatusTransitionException.class, 
            order::confirmOrder
        );
        assertTrue(exception.getMessage().contains("Cannot transition from Cancelled"));
    }

    @Test
    @DisplayName("Test order persistence in database")
    void testOrderPersistenceInDatabase() {
        OrderWithInventory order = new OrderWithInventory(1, "Jack", 
            Arrays.asList("Book", "Notebook"), Arrays.asList(2, 3), inventoryService);
        
        int orderId = orderRepository.saveOrder(order);
        assertTrue(orderId > 0);
        
        List<String> savedProducts = orderRepository.getOrderProducts(orderId);
        assertEquals(2, savedProducts.size());
        assertTrue(savedProducts.contains("Book"));
        assertTrue(savedProducts.contains("Notebook"));
    }

    @Test
    @DisplayName("Test concurrent order processing")
    void testConcurrentOrderProcessing() {
        OrderWithInventory order1 = new OrderWithInventory(1, "Kate", 
            Arrays.asList("Book"), Arrays.asList(4), inventoryService);
        OrderWithInventory order2 = new OrderWithInventory(2, "Leo", 
            Arrays.asList("Book"), Arrays.asList(3), inventoryService);
        
        order1.confirmOrder();
        assertEquals(6, inventoryService.getStock("Book"));
        
        order2.confirmOrder();
        assertEquals(3, inventoryService.getStock("Book"));
    }

    @Test
    @DisplayName("Test exact stock boundary")
    void testExactStockBoundary() {
        OrderWithInventory order = new OrderWithInventory(1, "Mike", 
            Arrays.asList("Book"), Arrays.asList(10), inventoryService);
        
        order.confirmOrder();
        assertEquals(0, inventoryService.getStock("Book"));
        assertEquals(OrderWithInventory.OrderStatus.CONFIRMED, order.getStatus());
    }
}
