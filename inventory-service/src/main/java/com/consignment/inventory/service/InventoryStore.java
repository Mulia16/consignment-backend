package com.consignment.inventory.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InventoryStore {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    @PostConstruct
    void seedData() {
        stock.put("SKU01", 120);
        stock.put("SKU02", 90);
        stock.put("SKU03", 60);
    }

    public int getAvailable(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    public synchronized boolean reserve(String sku, int quantity) {
        int current = getAvailable(sku);
        if (quantity <= 0 || current < quantity) {
            return false;
        }
        stock.put(sku, current - quantity);
        return true;
    }
}
