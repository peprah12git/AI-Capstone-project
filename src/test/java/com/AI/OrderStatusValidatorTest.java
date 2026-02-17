package com.AI;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

class OrderStatusValidatorTest {

    @Test
    void testCreatedToConfirmedAllowed() {
        assertDoesNotThrow(() -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CREATED, Order.OrderStatus.CONFIRMED));
    }

    @Test
    void testCreatedToCancelledAllowed() {
        assertDoesNotThrow(() -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CREATED, Order.OrderStatus.CANCELLED));
    }

    @Test
    void testCreatedToDeliveredNotAllowed() {
        assertThrows(IllegalStateException.class, () -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CREATED, Order.OrderStatus.DELIVERED));
    }

    @Test
    void testConfirmedToDeliveredAllowed() {
        assertDoesNotThrow(() -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CONFIRMED, Order.OrderStatus.DELIVERED));
    }

    @Test
    void testConfirmedToCancelledAllowed() {
        assertDoesNotThrow(() -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED));
    }

    @Test
    void testDeliveredToCancelledNotAllowed() {
        assertThrows(IllegalStateException.class, () -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.DELIVERED, Order.OrderStatus.CANCELLED));
    }

    @Test
    void testDeliveredNoTransitionsAllowed() {
        assertThrows(IllegalStateException.class, () -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.DELIVERED, Order.OrderStatus.CREATED));
    }

    @Test
    void testCancelledNoTransitionsAllowed() {
        assertThrows(IllegalStateException.class, () -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CANCELLED, Order.OrderStatus.CREATED));
    }

    @Test
    void testSameStatusNotAllowed() {
        assertThrows(IllegalStateException.class, () -> 
            OrderStatusValidator.validateTransition(Order.OrderStatus.CREATED, Order.OrderStatus.CREATED));
    }

    @Test
    void testIsTransitionAllowed() {
        assertTrue(OrderStatusValidator.isTransitionAllowed(Order.OrderStatus.CREATED, Order.OrderStatus.CONFIRMED));
        assertFalse(OrderStatusValidator.isTransitionAllowed(Order.OrderStatus.DELIVERED, Order.OrderStatus.CANCELLED));
    }

    @Test
    void testGetAllowedNextStates() {
        Set<Order.OrderStatus> createdNext = OrderStatusValidator.getAllowedNextStates(Order.OrderStatus.CREATED);
        assertEquals(2, createdNext.size());
        assertTrue(createdNext.contains(Order.OrderStatus.CONFIRMED));
        assertTrue(createdNext.contains(Order.OrderStatus.CANCELLED));

        Set<Order.OrderStatus> deliveredNext = OrderStatusValidator.getAllowedNextStates(Order.OrderStatus.DELIVERED);
        assertTrue(deliveredNext.isEmpty());
    }
}
