package com.AI;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OrderStatusValidator {
    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(Order.OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(Order.OrderStatus.CREATED, 
            EnumSet.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(Order.OrderStatus.CONFIRMED, 
            EnumSet.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(Order.OrderStatus.DELIVERED, 
            EnumSet.noneOf(Order.OrderStatus.class));
        ALLOWED_TRANSITIONS.put(Order.OrderStatus.CANCELLED, 
            EnumSet.noneOf(Order.OrderStatus.class));
    }

    public static void validateTransition(Order.OrderStatus current, Order.OrderStatus next) {
        if (current == next) {
            throw new IllegalStateException("Order is already in " + current + " status");
        }

        Set<Order.OrderStatus> allowedNextStates = ALLOWED_TRANSITIONS.get(current);
        if (!allowedNextStates.contains(next)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s. Allowed transitions: %s", 
                    current, next, allowedNextStates));
        }
    }

    public static boolean isTransitionAllowed(Order.OrderStatus current, Order.OrderStatus next) {
        return current != next && ALLOWED_TRANSITIONS.get(current).contains(next);
    }

    public static Set<Order.OrderStatus> getAllowedNextStates(Order.OrderStatus current) {
        return EnumSet.copyOf(ALLOWED_TRANSITIONS.get(current));
    }
}
