package com.consignment.service.api;

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneeDashboardResponse;
import com.consignment.service.model.consignee.ConsigneeProductResponse;
import com.consignment.service.model.consignee.ConsigneePurchaseOrderResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ConsigneePurchaseOrderEntity;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consignee")
public class ConsigneeController {

    private static final Logger log = LoggerFactory.getLogger(ConsigneeController.class);

    private final ConsigneeContext consigneeContext;
    private final ConsignmentSetupMapper consignmentSetupMapper;
    private final ConsigneePurchaseOrderMapper consigneePurchaseOrderMapper;
    private final CsoMapper csoMapper;

    public ConsigneeController(ConsigneeContext consigneeContext,
                               ConsignmentSetupMapper consignmentSetupMapper,
                               ConsigneePurchaseOrderMapper consigneePurchaseOrderMapper,
                               CsoMapper csoMapper) {
        this.consigneeContext = consigneeContext;
        this.consignmentSetupMapper = consignmentSetupMapper;
        this.consigneePurchaseOrderMapper = consigneePurchaseOrderMapper;
        this.csoMapper = csoMapper;
    }

    /**
     * GET /api/consignee/products
     * Returns all consignment items assigned to the store from the JWT claim.
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ConsigneeProductResponse>>> getProducts() {
        String store = consigneeContext.requireStore();

        List<ExternalSupplierEntity> entities = consignmentSetupMapper.findByConsigneeStore(store);

        List<ConsigneeProductResponse> products = entities.stream()
                .map(e -> new ConsigneeProductResponse(
                        e.getItemCode(),
                        e.getItemName(),
                        e.getVariant(),
                        e.getSupplierCode(),
                        e.getSupplierContract(),
                        e.getConsigneeStore()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * GET /api/consignee/purchase-orders
     * Returns all purchase orders for the store from the JWT claim.
     */
    @GetMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<List<ConsigneePurchaseOrderResponse>>> getPurchaseOrders() {
        String store = consigneeContext.requireStore();

        List<ConsigneePurchaseOrderEntity> entities = consigneePurchaseOrderMapper.findByStore(store);

        List<ConsigneePurchaseOrderResponse> purchaseOrders = entities.stream()
                .map(e -> new ConsigneePurchaseOrderResponse(
                        e.getPoNumber(),
                        e.getStore(),
                        e.getItemCode(),
                        e.getItemName(),
                        e.getOrderedQty(),
                        e.getStatus(),
                        e.getPoDate()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(purchaseOrders));
    }

    /**
     * POST /api/consignee/purchase-orders/sync
     * Stub endpoint for triggering HCMM PO sync.
     * Requirements: 10.1, 10.2, 10.3
     */
    @PostMapping("/purchase-orders/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncPurchaseOrders() {
        log.info("[{}] HCMM PO sync triggered (stub)", Instant.now());
        Map<String, Object> body = Map.of("message", "Sync triggered (stub)", "synced", 0);
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    /**
     * GET /api/consignee/dashboard
     * Returns sales qty and current stock qty per item for the store from the JWT claim.
     * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<ConsigneeDashboardResponse>>> getDashboard() {
        String store = consigneeContext.requireStore();

        // Get items with currentInventoryQty (= currentStockQty) for this store
        List<ExternalSupplierEntity> items = consignmentSetupMapper.findByConsigneeStore(store);

        // Build a map of itemCode -> salesQty from CSO/CSDO transactions
        List<Map<String, Object>> salesRows = csoMapper.findSalesQtyByStore(store);
        Map<String, Integer> salesQtyByItem = new java.util.HashMap<>();
        for (Map<String, Object> row : salesRows) {
            String itemCode = (String) row.get("item_code");
            Number qty = (Number) row.get("sales_qty");
            salesQtyByItem.put(itemCode, qty != null ? qty.intValue() : 0);
        }

        List<ConsigneeDashboardResponse> dashboard = items.stream()
                .map(e -> new ConsigneeDashboardResponse(
                        e.getItemCode(),
                        e.getItemName(),
                        salesQtyByItem.getOrDefault(e.getItemCode(), 0),
                        e.getCurrentInventoryQty(),
                        store
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
