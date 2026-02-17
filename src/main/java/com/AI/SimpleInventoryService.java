package com.AI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleInventoryService implements InventoryService {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addStock(String product, Integer quantity) {
        inventory.put(product, inventory.getOrDefault(product, 0) + quantity);
    }

    @Override
    public boolean checkStock(List<String> products, List<Integer> quantities) {
        for (int i = 0; i < products.size(); i++) {
            Integer available = inventory.getOrDefault(products.get(i), 0);
            if (available < quantities.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void reserveStock(List<String> products, List<Integer> quantities) {
        for (int i = 0; i < products.size(); i++) {
            String product = products.get(i);
            Integer quantity = quantities.get(i);
            Integer available = inventory.getOrDefault(product, 0);
            inventory.put(product, available - quantity);
        }
    }

    @Override
    public void returnStock(List<String> products, List<Integer> quantities) {
        for (int i = 0; i < products.size(); i++) {
            String product = products.get(i);
            Integer quantity = quantities.get(i);
            inventory.put(product, inventory.getOrDefault(product, 0) + quantity);
        }
    }

    public Integer getStock(String product) {
        return inventory.getOrDefault(product, 0);
    }
}
