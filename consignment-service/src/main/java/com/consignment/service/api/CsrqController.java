package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csrq.CsrqRequest;
import com.consignment.service.model.csrq.CsrqResponse;
import com.consignment.service.model.csrq.CsrqSearchCriteria;
import com.consignment.service.model.csrq.CsrqUpdateRequest;
import com.consignment.service.service.CsrqService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csrq")
public class CsrqController {

    private final CsrqService csrqService;

    public CsrqController(CsrqService csrqService) {
        this.csrqService = csrqService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsrqResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String internalSupplierStore,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csrqService.search(new CsrqSearchCriteria(docNo, company, store, supplierCode, supplierContract, branch, internalSupplierStore, createdMethod, referenceNo, itemCode, status, createdBy, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CsrqResponse>> create(@Valid @RequestBody CsrqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSRQ created", csrqService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrqResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CsrqUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrqService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrqResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrqService.getById(id)));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<CsrqResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrqService.release(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        csrqService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("CSRQ deleted", null));
    }
}
