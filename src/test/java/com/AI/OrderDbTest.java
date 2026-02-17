package com.AI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderDbTest {
    private DatabaseInventoryService inventoryService;
    private OrderRepository orderRepository;

    @BeforeAll
    void setup() {
        DatabaseInitializer.setDatabaseUrl("jdbc:sqlite::memory:");
        DatabaseInitializer.initialize();
        inventoryService = new DatabaseInventoryService();
        orderRepository = new OrderRepository();
    }

    @AfterAll
    void tearDown() throws Exception {
        DatabaseInitializer.closeSharedConnection();
    }

    @BeforeEach
    void resetInventory() throws Exception {
        Connection conn = DatabaseInitializer.getConnection();
        PreparedStatement pstmt = conn.prepareStatement("UPDATE inventory SET stock = ? WHERE product = ?");
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

    @Test
    void testValidTransitionCreatedToConfirmedToDelivered() {
        OrderWithInventory order = new OrderWithInventory(1, "Customer", 
            Arrays.asList("Book"), Arrays.asList(3), inventoryService);
        
        assertEquals(OrderWithInventory.OrderStatus.CREATED, order.getStatus());
        
        order.confirmOrder();
        assertEquals(OrderWithInventory.OrderStatus.CONFIRMED, order.getStatus());
        
        order.deliverOrder();
        assertEquals(OrderWithInventory.OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void testInvalidTransitionDeliveredToCancelled() {
        OrderWithInventory order = new OrderWithInventory(1, "Customer", 
            Arrays.asList("Book"), Arrays.asList(3), inventoryService);
        
        order.confirmOrder();
        order.deliverOrder();
        
        assertThrows(InvalidStatusTransitionException.class, order::cancelOrder);
    }

    @Test
    void testCannotConfirmWithInsufficientStock() {
        OrderWithInventory order = new OrderWithInventory(1, "Customer", 
            Arrays.asList("Book"), Arrays.asList(15), inventoryService);
        
        assertThrows(InsufficientInventoryException.class, order::confirmOrder);
        assertEquals(OrderWithInventory.OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void testCancellationRestoresStockIfConfirmed() {
        OrderWithInventory order = new OrderWithInventory(1, "Customer", 
            Arrays.asList("Book"), Arrays.asList(5), inventoryService);
        
        assertEquals(10, inventoryService.getStock("Book"));
        
        order.confirmOrder();
        assertEquals(5, inventoryService.getStock("Book"));
        
        order.cancelOrder();
        assertEquals(10, inventoryService.getStock("Book"));
    }
}
