package com.consignment.service.client;

import com.consignment.service.client.model.InventoryAvailability;
import com.consignment.service.client.model.InventoryReserveRequest;
import com.consignment.service.client.model.InventoryReserveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/{sku}/availability")
    InventoryAvailability getAvailability(@PathVariable("sku") String sku);

    @PostMapping("/api/v1/inventory/reserve")
    InventoryReserveResponse reserve(@RequestBody InventoryReserveRequest request);
}
