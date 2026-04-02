package com.consignment.inventory.api;

import com.consignment.inventory.model.AvailabilityResponse;
import com.consignment.inventory.model.ReserveRequest;
import com.consignment.inventory.model.ReserveResponse;
import com.consignment.inventory.service.InventoryStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryStore inventoryStore;

    public InventoryController(InventoryStore inventoryStore) {
        this.inventoryStore = inventoryStore;
    }

    @GetMapping("/{sku}/availability")
    public AvailabilityResponse getAvailability(@PathVariable String sku) {
        int available = inventoryStore.getAvailable(sku);
        return new AvailabilityResponse(sku, available);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(@Valid @RequestBody ReserveRequest request) {
        boolean reserved = inventoryStore.reserve(request.sku(), request.quantity());
        int remaining = inventoryStore.getAvailable(request.sku());
        return new ReserveResponse(request.sku(), request.quantity(), reserved, remaining);
    }
}
