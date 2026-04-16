package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.billing.*;
import com.consignment.service.pdf.SupplierBillingSlipService;
import com.consignment.service.service.SupplierBillingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class SupplierBillingController {

    private final SupplierBillingService service;
    private final SupplierBillingSlipService slipService;

    public SupplierBillingController(SupplierBillingService service, SupplierBillingSlipService slipService) {
        this.service = service;
        this.slipService = slipService;
    }

    /**
     * Compute and create a new Supplier Consignment Billing Request (SCBR).
     * Reads unsettled consignment_unpost for the supplier+store+period,
     * aggregates by SKU, calculates billingQty = salesQty - salesReturnQty + bfQty.
     * If carryForwardDecimal=true, decimal portion is moved to CF_Qty for next period.
     * Prevents duplicate: throws 400 if SCBR already exists for same supplier+period.
     *
     * Flow:
     * 1. POST /api/supplier-billing/compute → returns SCBR id + details[].id
     * 2. GET  /api/supplier-billing/{id}    → retrieve detail to review before release
     * 3. PUT  /api/supplier-billing/{id}/release → release SCBR (HELD → RELEASED)
     *
     * Reprocess: DELETE /api/supplier-billing/{id} → recompute
     */
    @PostMapping("/api/supplier-billing/compute")
    public ResponseEntity<ApiResponse<SupplierBillingResponse>> compute(
            @Valid @RequestBody SupplierBillingComputeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier billing request computed",
                        service.compute(request)));
    }

    /**
     * List / search SCBR documents with optional filters.
     * Supports filter by: docNo, company, store, supplierCode, supplierContract,
     * periodType, status, processStatus, fromDate, toDate.
     */
    @GetMapping("/api/supplier-billing")
    public ResponseEntity<ApiResponse<List<SupplierBillingResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String processStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = service.search(new SupplierBillingSearchCriteria(
                docNo, company, store, supplierCode, supplierContract,
                periodType, status, processStatus, fromDate, toDate, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    /** Get SCBR detail by ID — includes header and all item lines. */
    @GetMapping("/api/supplier-billing/{id}")
    public ResponseEntity<ApiResponse<SupplierBillingResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    /**
     * Release SCBR (HELD → RELEASED).
     * After release:
     * - Marks consignment_unpost rows as settled
     * - Deducts customer_consignment_inventory
     * - Deducts supplier_book_value_inventory
     */
    @PutMapping("/api/supplier-billing/{id}/release")
    public ResponseEntity<ApiResponse<SupplierBillingResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.release(id)));
    }

    /**
     * Delete SCBR (HELD only).
     * After delete, unpost rows remain unsettled — user can recompute.
     */
    @DeleteMapping("/api/supplier-billing/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Supplier billing request deleted", null));
    }

    @GetMapping("/api/supplier-billing/{id}/slip")
    public ResponseEntity<byte[]> printSlip(@PathVariable String id) {
        byte[] pdf = slipService.generate(id);
        String filename = "SCBR-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Filename", filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
