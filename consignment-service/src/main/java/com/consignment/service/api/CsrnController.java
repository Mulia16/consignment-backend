package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csrn.*;
import com.consignment.service.service.CsrnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csrn")
public class CsrnController {

    private final CsrnService csrnService;

    public CsrnController(CsrnService csrnService) {
        this.csrnService = csrnService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsrnResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String internalSupplierStore,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String reasonCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csrnService.search(new CsrnSearchCriteria(
                docNo, company, store, supplierCode, supplierContract, internalSupplierStore,
                status, createdBy, referenceNo, reasonCode, itemCode, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrnResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CsrnResponse>> create(@Valid @RequestBody CsrnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSRN created", csrnService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrnResponse>> update(
            @PathVariable String id, @Valid @RequestBody CsrnUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.update(id, request)));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<CsrnResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.release(id)));
    }

    @PutMapping("/{id}/detail/{detailId}/actual-qty")
    public ResponseEntity<ApiResponse<CsrnResponse>> updateActualQty(
            @PathVariable String id, @PathVariable String detailId,
            @Valid @RequestBody CsrnActualQtyUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.updateActualQty(id, detailId, request)));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<CsrnResponse>> complete(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.complete(id)));
    }
}
