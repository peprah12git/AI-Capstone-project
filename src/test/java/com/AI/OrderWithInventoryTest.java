package com.AI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;

class OrderWithInventoryTest {
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = Mockito.mock(InventoryService.class);
    }

    @Test
    void testOrderCreation() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(2), inventoryService);
        assertEquals(1, order.getOrderId());
        assertEquals("John Doe", order.getCustomerName());
        assertEquals(OrderWithInventory.OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void testConfirmOrderReservesInventory() {
        List<String> products = Arrays.asList("Product1", "Product2");
        List<Integer> quantities = Arrays.asList(2, 3);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", products, quantities, inventoryService);
        
        order.confirmOrder();
        
        assertEquals(OrderWithInventory.OrderStatus.CONFIRMED, order.getStatus());
        verify(inventoryService, times(1)).reserveStock(products, quantities);
    }

    @Test
    void testDeliverOrder() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(2), inventoryService);
        order.confirmOrder();
        order.deliverOrder();
        
        assertEquals(OrderWithInventory.OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void testCancelFromCreatedNoInventoryReturn() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(2), inventoryService);
        
        order.cancelOrder();
        
        assertEquals(OrderWithInventory.OrderStatus.CANCELLED, order.getStatus());
        verify(inventoryService, never()).returnStock(any(), any());
    }

    @Test
    void testCancelFromConfirmedReturnsInventory() {
        List<String> products = Arrays.asList("Product1");
        List<Integer> quantities = Arrays.asList(2);
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", products, quantities, inventoryService);
        order.confirmOrder();
        
        order.cancelOrder();
        
        assertEquals(OrderWithInventory.OrderStatus.CANCELLED, order.getStatus());
        verify(inventoryService, times(1)).returnStock(products, quantities);
    }

    @Test
    void testCannotDeliverFromCreated() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(2), inventoryService);
        
        assertThrows(InvalidStatusTransitionException.class, order::deliverOrder);
    }

    @Test
    void testCannotTransitionFromDelivered() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(2), inventoryService);
        order.confirmOrder();
        order.deliverOrder();
        
        assertThrows(InvalidStatusTransitionException.class, order::cancelOrder);
    }

    @Test
    void testCannotTransitionFromCancelled() {
        OrderWithInventory order = new OrderWithInventory(1, "John Doe", 
            Arrays.asList("Product1"), Arrays.asList(2), inventoryService);
        order.cancelOrder();
        
        assertThrows(InvalidStatusTransitionException.class, order::confirmOrder);
    }

    @Test
    void testInvalidOrderId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderWithInventory(0, "John Doe", Arrays.asList("Product1"), 
                Arrays.asList(2), inventoryService));
    }

    @Test
    void testEmptyCustomerName() {
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderWithInventory(1, "", Arrays.asList("Product1"), 
                Arrays.asList(2), inventoryService));
    }

    @Test
    void testEmptyProducts() {
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderWithInventory(1, "John Doe", Arrays.asList(), 
                Arrays.asList(), inventoryService));
    }

    @Test
    void testInvalidQuantity() {
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderWithInventory(1, "John Doe", Arrays.asList("Product1"), 
                Arrays.asList(0), inventoryService));
    }

    @Test
    void testMismatchedArrayLengths() {
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderWithInventory(1, "John Doe", Arrays.asList("Product1", "Product2"), 
                Arrays.asList(2), inventoryService));
    }
}
