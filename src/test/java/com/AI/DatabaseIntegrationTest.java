package com.AI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseIntegrationTest {
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
    void testInitialInventory() {
        assertEquals(10, inventoryService.getStock("Book"));
        assertEquals(50, inventoryService.getStock("Pen"));
        assertEquals(20, inventoryService.getStock("Notebook"));
    }

    @Test
    void testCheckStockSufficient() {
        assertTrue(inventoryService.checkStock(Arrays.asList("Book"), Arrays.asList(5)));
        assertTrue(inventoryService.checkStock(Arrays.asList("Pen", "Notebook"), Arrays.asList(10, 5)));
    }

    @Test
    void testCheckStockInsufficient() {
        assertFalse(inventoryService.checkStock(Arrays.asList("Book"), Arrays.asList(15)));
        assertFalse(inventoryService.checkStock(Arrays.asList("Pen", "Notebook"), Arrays.asList(10, 25)));
    }

    @Test
    void testReserveStock() {
        inventoryService.reserveStock(Arrays.asList("Book"), Arrays.asList(3));
        assertEquals(7, inventoryService.getStock("Book"));
    }

    @Test
    void testReturnStock() {
        inventoryService.reserveStock(Arrays.asList("Pen"), Arrays.asList(10));
        assertEquals(40, inventoryService.getStock("Pen"));
        
        inventoryService.returnStock(Arrays.asList("Pen"), Arrays.asList(10));
        assertEquals(50, inventoryService.getStock("Pen"));
    }

    @Test
    void testSaveOrder() {
        OrderWithInventory order = new OrderWithInventory(1, "Alice", 
            Arrays.asList("Book", "Pen"), Arrays.asList(2, 5), inventoryService);
        
        int orderId = orderRepository.saveOrder(order);
        assertTrue(orderId > 0);
    }

    @Test
    void testUpdateOrderStatus() {
        OrderWithInventory order = new OrderWithInventory(1, "Bob", 
            Arrays.asList("Notebook"), Arrays.asList(3), inventoryService);
        
        int orderId = orderRepository.saveOrder(order);
        orderRepository.updateOrderStatus(orderId, OrderWithInventory.OrderStatus.CONFIRMED);
        
        assertDoesNotThrow(() -> orderRepository.updateOrderStatus(orderId, OrderWithInventory.OrderStatus.DELIVERED));
    }

    @Test
    void testFullOrderWorkflow() {
        OrderWithInventory order = new OrderWithInventory(1, "Charlie", 
            Arrays.asList("Book"), Arrays.asList(3), inventoryService);
        
        int orderId = orderRepository.saveOrder(order);
        assertEquals(10, inventoryService.getStock("Book"));
        
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(7, inventoryService.getStock("Book"));
        
        order.deliverOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(OrderWithInventory.OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void testCancelOrderReturnsStock() {
        OrderWithInventory order = new OrderWithInventory(1, "Diana", 
            Arrays.asList("Pen"), Arrays.asList(10), inventoryService);
        
        int orderId = orderRepository.saveOrder(order);
        order.confirmOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(40, inventoryService.getStock("Pen"));
        
        order.cancelOrder();
        orderRepository.updateOrderStatus(orderId, order.getStatus());
        assertEquals(50, inventoryService.getStock("Pen"));
    }
}
