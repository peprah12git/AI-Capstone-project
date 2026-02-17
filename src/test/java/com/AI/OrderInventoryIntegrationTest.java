package com.AI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

class OrderInventoryIntegrationTest {
    private SimpleInventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new SimpleInventoryService();
    }

    @Test
    void testConfirmOrderWithSufficientInventory() {
        inventoryService.addStock("Product1", 10);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(5), inventoryService);
        
        order.confirmOrder();
        
        assertEquals(OrderWithInventory.OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(5, inventoryService.getStock("Product1"));
    }

    @Test
    void testConfirmOrderWithInsufficientInventory() {
        inventoryService.addStock("Product1", 3);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(5), inventoryService);
        
        assertThrows(InsufficientInventoryException.class, order::confirmOrder);
        assertEquals(OrderWithInventory.OrderStatus.CREATED, order.getStatus());
        assertEquals(3, inventoryService.getStock("Product1"));
    }

    @Test
    void testConfirmOrderWithNoInventory() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(5), inventoryService);
        
        assertThrows(InsufficientInventoryException.class, order::confirmOrder);
    }

    @Test
    void testCancelConfirmedOrderReturnsStock() {
        inventoryService.addStock("Product1", 10);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(5), inventoryService);
        order.confirmOrder();
        
        order.cancelOrder();
        
        assertEquals(OrderWithInventory.OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, inventoryService.getStock("Product1"));
    }

    @Test
    void testMultipleProductsInventoryCheck() {
        inventoryService.addStock("Product1", 10);
        inventoryService.addStock("Product2", 5);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1", "Product2"), Arrays.asList(3, 2), inventoryService);
        
        order.confirmOrder();
        
        assertEquals(7, inventoryService.getStock("Product1"));
        assertEquals(3, inventoryService.getStock("Product2"));
    }

    @Test
    void testMultipleProductsInsufficientInventory() {
        inventoryService.addStock("Product1", 10);
        inventoryService.addStock("Product2", 1);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1", "Product2"), Arrays.asList(3, 2), inventoryService);
        
        assertThrows(InsufficientInventoryException.class, order::confirmOrder);
        assertEquals(10, inventoryService.getStock("Product1"));
        assertEquals(1, inventoryService.getStock("Product2"));
    }

    @Test
    void testInvalidTransitionThrowsCustomException() {
        inventoryService.addStock("Product1", 10);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(5), inventoryService);
        
        assertThrows(InvalidStatusTransitionException.class, order::deliverOrder);
    }

    @Test
    void testCannotCancelDeliveredOrder() {
        inventoryService.addStock("Product1", 10);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(5), inventoryService);
        order.confirmOrder();
        order.deliverOrder();
        
        assertThrows(InvalidStatusTransitionException.class, order::cancelOrder);
        assertEquals(5, inventoryService.getStock("Product1"));
    }

    @Test
    void testFullOrderLifecycleWithInventory() {
        inventoryService.addStock("Product1", 20);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(8), inventoryService);
        
        assertEquals(20, inventoryService.getStock("Product1"));
        
        order.confirmOrder();
        assertEquals(12, inventoryService.getStock("Product1"));
        
        order.deliverOrder();
        assertEquals(12, inventoryService.getStock("Product1"));
        assertEquals(OrderWithInventory.OrderStatus.DELIVERED, order.getStatus());
    }
}
