package com.AI;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

class OrderTest {

    @Test
    void testValidOrderCreation() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        assertEquals(1, order.getOrderId());
        assertEquals("John Doe", order.getCustomerName());
        assertEquals(Order.OrderStatus.CREATED, order.getStatus());
        assertNotNull(order.getMetadata().getCreatedAt());
    }

    @Test
    void testConfirmOrder() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        order.confirmOrder();
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(Order.OrderStatus.CREATED, order.getPreviousStatus());
        assertNotNull(order.getMetadata().getConfirmedAt());
    }

    @Test
    void testDeliverOrder() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        order.confirmOrder();
        order.deliverOrder();
        assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
        assertEquals(Order.OrderStatus.CONFIRMED, order.getPreviousStatus());
        assertNotNull(order.getMetadata().getDeliveredAt());
    }

    @Test
    void testCancelOrderFromCreated() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        order.cancelOrder();
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getMetadata().getCancelledAt());
    }

    @Test
    void testCancelOrderFromConfirmed() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        order.confirmOrder();
        order.cancelOrder();
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testCannotDeliverBeforeConfirming() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        assertThrows(IllegalStateException.class, order::deliverOrder);
    }

    @Test
    void testCannotCancelAfterDelivery() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        order.confirmOrder();
        order.deliverOrder();
        assertThrows(IllegalStateException.class, order::cancelOrder);
    }

    @Test
    void testInvalidOrderId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(0, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED));
    }

    @Test
    void testInvalidCustomerName() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(1, "", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED));
    }

    @Test
    void testEmptyProductList() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(1, "John Doe", Arrays.asList(), Arrays.asList(), Order.OrderStatus.CREATED));
    }

    @Test
    void testMismatchedArrayLengths() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(1, "John Doe", Arrays.asList("Product1", "Product2"), Arrays.asList(2), Order.OrderStatus.CREATED));
    }

    @Test
    void testInvalidQuantity() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(0), Order.OrderStatus.CREATED));
    }

    @Test
    void testMultipleProducts() {
        List<String> products = Arrays.asList("Product1", "Product2", "Product3");
        List<Integer> quantities = Arrays.asList(1, 2, 3);
        Order order = new Order(1, "John Doe", products, quantities, Order.OrderStatus.CREATED);
        assertEquals(3, order.getProductList().size());
        assertEquals(3, order.getQuantityList().size());
    }

    @Test
    void testFullOrderLifecycle() {
        Order order = new Order(1, "John Doe", Arrays.asList("Product1"), Arrays.asList(2), Order.OrderStatus.CREATED);
        order.confirmOrder();
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        order.deliverOrder();
        assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getMetadata().getCreatedAt());
        assertNotNull(order.getMetadata().getConfirmedAt());
        assertNotNull(order.getMetadata().getDeliveredAt());
    }
}
