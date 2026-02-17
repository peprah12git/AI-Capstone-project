package com.AI;

import java.util.List;

public interface InventoryService {
    void reserveStock(List<String> products, List<Integer> quantities);
    void returnStock(List<String> products, List<Integer> quantities);
    boolean checkStock(List<String> products, List<Integer> quantities);
}
