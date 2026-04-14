package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.billing.*;
import com.consignment.service.service.CustomerBillingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class CustomerBillingController {

    private final CustomerBillingService service;

    public CustomerBillingController(CustomerBillingService service) {
        this.service = service;
    }

    // ── ACMM Inbound: receive unpost sales (POS / B2B / Online) ──────────────

    /**
     * Called by ACMM to push unpost sales/return data into consignment module.
     * Source types: POS, B2B, ONLINE
     */
    @PostMapping("/api/acmm/unpost-sales/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncUnpostSales(
            @Valid @RequestBody UnpostSalesSyncRequest request) {
        int count = service.syncUnpostSales(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Unpost sales synced", Map.of("synced", count)));
    }

    // ── Customer Billing Request ──────────────────────────────────────────────

    /**
     * Compute and create a new Customer Consignment Billing Request (CBR).
     * Reads unsettled consignment_unpost for the store+period,
     * aggregates by SKU, calculates billingQty = salesQty - returnQty.
     * Prevents duplicate: throws 400 if CBR already exists for same store+period.
     *
     * <p>Recommended flow:
     * <ol>
     *   <li>POST /api/customer-billing/compute → returns CBR header id + details[].id (detailId)</li>
     *   <li>GET  /api/customer-billing/{id}    → alternatively retrieve detailId from here</li>
     *   <li>PUT  /api/customer-billing/{id}/release → release CBR (HELD → RELEASED)</li>
     *   <li>PUT  /api/customer-billing/{id}/detail/{detailId}/actual-return-qty → update actual return qty after release</li>
     * </ol>
     *
     * <p>Reprocess flow (if CBR needs to be regenerated):
     * <ol>
     *   <li>DELETE /api/customer-billing/{id} → delete existing HELD CBR</li>
     *   <li>POST   /api/customer-billing/compute → recompute for same store+period</li>
     * </ol>
     */
    @PostMapping("/api/customer-billing/compute")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> compute(
            @Valid @RequestBody CustomerBillingComputeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer billing request computed",
                        service.compute(request)));
    }

    /** List / search CBR documents */
    @GetMapping("/api/customer-billing")
    public ResponseEntity<ApiResponse<List<CustomerBillingResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerBranch,
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String processStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = service.search(new CustomerBillingSearchCriteria(
                docNo, company, store, customerCode, customerBranch, periodType,
                status, processStatus, fromDate, toDate, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    /**
     * List failed CBR records (processStatus = FAILED).
     * Used for the "Failed Records" screen — supports filter by company, store, customer.
     * User can then Confirm or Delete each record.
     */
    @GetMapping("/api/customer-billing/failed")
    public ResponseEntity<ApiResponse<List<CustomerBillingResponse>>> searchFailed(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = service.searchFailed(new CustomerBillingSearchCriteria(
                null, company, store, customerCode, null, null,
                null, null, fromDate, toDate, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    /** Get CBR detail by ID */
    @GetMapping("/api/customer-billing/{id}")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    /**
     * Confirm a FAILED CBR record — acknowledges the failure and marks it as COMPLETED.
     * Used from the "Failed Records" screen Confirm button.
     */
    @PutMapping("/api/customer-billing/{id}/confirm-failed")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> confirmFailed(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.confirmFailed(id)));
    }

    /**
     * Release CBR (HELD → RELEASED).
     * After release: marks unpost rows as settled so they won't be included in next CBR.
     */
    @PutMapping("/api/customer-billing/{id}/release")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.release(id)));
    }

    /** Update actual return qty on a detail line (RELEASED only) */
    @PutMapping("/api/customer-billing/{id}/detail/{detailId}/actual-return-qty")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> updateActualReturnQty(
            @PathVariable String id,
            @PathVariable String detailId,
            @Valid @RequestBody CustomerBillingActualReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updateActualReturnQty(id, detailId, request)));
    }

    /**
     * Delete CBR (HELD only, non-failed).
     * After delete, unpost rows remain unsettled — user can recompute (reprocess).
     * Note: FAILED records must use DELETE /api/customer-billing/{id}/failed
     */
    @DeleteMapping("/api/customer-billing/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer billing request deleted", null));
    }

    /**
     * Delete a FAILED CBR record (processStatus = FAILED only).
     * Used from the "Failed Records" screen Delete button.
     */
    @DeleteMapping("/api/customer-billing/{id}/failed")
    public ResponseEntity<ApiResponse<Void>> deleteFailed(@PathVariable String id) {
        service.deleteFailed(id);
        return ResponseEntity.ok(ApiResponse.ok("Failed billing request deleted", null));
    }
}
