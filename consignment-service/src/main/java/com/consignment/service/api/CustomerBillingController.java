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

@RestController
@RequestMapping("/api/customer-billing")
public class CustomerBillingController {

    private final CustomerBillingService service;

    public CustomerBillingController(CustomerBillingService service) {
        this.service = service;
    }

    /** Compute and create a new customer consignment billing request */
    @PostMapping("/compute")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> compute(
            @Valid @RequestBody CustomerBillingComputeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer billing request computed", service.compute(request)));
    }

    /** List / search billing requests */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerBillingResponse>>> search(
            @RequestParam(required = false) String docNo,
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
                docNo, store, customerCode, customerBranch, periodType,
                status, processStatus, fromDate, toDate, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    /** Get billing request detail */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    /** Release billing request (HELD → RELEASED) */
    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.release(id)));
    }

    /** Update actual return qty on a detail line (RELEASED only) */
    @PutMapping("/{id}/detail/{detailId}/actual-return-qty")
    public ResponseEntity<ApiResponse<CustomerBillingResponse>> updateActualReturnQty(
            @PathVariable String id,
            @PathVariable String detailId,
            @Valid @RequestBody CustomerBillingActualReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateActualReturnQty(id, detailId, request)));
    }

    /** Delete billing request (HELD only) */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer billing request deleted", null));
    }
}
