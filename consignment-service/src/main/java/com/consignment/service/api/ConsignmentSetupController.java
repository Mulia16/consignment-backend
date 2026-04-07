package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.setup.*;
import com.consignment.service.service.ConsignmentSetupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consignment-setup")
public class ConsignmentSetupController {

    private final ConsignmentSetupService service;

    public ConsignmentSetupController(ConsignmentSetupService service) {
        this.service = service;
    }

    // ── Item Setup CRUD ────────────────────────────────────────────────────────

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ConsignmentSetupItemResponse>>> listItems(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String variant,
            @RequestParam(required = false) String hierarchy,
            @RequestParam(required = false) String categoryL1,
            @RequestParam(required = false) String categoryL2,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var criteria = new ItemSetupSearchCriteria(itemCode, itemName, variant, hierarchy, categoryL1, categoryL2, page, perPage);
        var result = service.listItems(criteria);
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/item/{itemCode}")
    public ResponseEntity<ApiResponse<ConsignmentSetupItemResponse>> getByItemCode(@PathVariable String itemCode) {
        return ResponseEntity.ok(ApiResponse.success(service.getByItemCode(itemCode)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<ConsignmentSetupItemResponse>> createItem(
            @Valid @RequestBody ItemSetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item setup created", service.createOrUpdateItem(request)));
    }

    @PutMapping("/item/{itemCode}")
    public ResponseEntity<ApiResponse<ConsignmentSetupItemResponse>> updateItem(
            @PathVariable String itemCode,
            @Valid @RequestBody ItemSetupRequest request) {
        // ensure path itemCode matches body
        var merged = new ItemSetupRequest(itemCode, request.itemName(), request.variant(),
                request.hierarchy(), request.itemModel(), request.unitRetail(), request.mvc(),
                request.categoryL1(), request.categoryL2(), request.categoryL3());
        return ResponseEntity.ok(ApiResponse.ok("Item setup updated", service.createOrUpdateItem(merged)));
    }

    // ── External Supplier ──────────────────────────────────────────────────────

    @PostMapping("/item/{itemCode}/external-supplier")
    public ResponseEntity<ApiResponse<ExternalSupplierSetupResponse>> createExternalSupplier(
            @PathVariable String itemCode,
            @Valid @RequestBody ExternalSupplierSetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("External supplier added", service.addExternalSupplier(itemCode, request)));
    }

    @PutMapping("/item/{itemCode}/external-supplier/{id}")
    public ResponseEntity<ApiResponse<ExternalSupplierSetupResponse>> updateExternalSupplier(
            @PathVariable String itemCode,
            @PathVariable String id,
            @Valid @RequestBody ExternalSupplierSetupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateExternalSupplier(itemCode, id, request)));
    }

    @DeleteMapping("/item/{itemCode}/external-supplier/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExternalSupplier(
            @PathVariable String itemCode,
            @PathVariable String id) {
        service.deleteExternalSupplier(itemCode, id);
        return ResponseEntity.ok(ApiResponse.ok("External supplier deleted", null));
    }

    // ── Internal Supplier ──────────────────────────────────────────────────────

    @PostMapping("/item/{itemCode}/internal-supplier")
    public ResponseEntity<ApiResponse<InternalSupplierSetupResponse>> createInternalSupplier(
            @PathVariable String itemCode,
            @Valid @RequestBody InternalSupplierSetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Internal supplier added", service.addInternalSupplier(itemCode, request)));
    }
}
