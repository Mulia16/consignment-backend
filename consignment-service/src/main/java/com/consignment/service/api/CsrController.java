package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csr.CsrActualQtyUpdateRequest;
import com.consignment.service.model.csr.CsrRequest;
import com.consignment.service.model.csr.CsrResponse;
import com.consignment.service.model.csr.CsrSearchCriteria;
import com.consignment.service.model.csr.CsrUpdateRequest;
import com.consignment.service.service.CsrService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csr")
public class CsrController {

    private final CsrService csrService;

    public CsrController(CsrService csrService) {
        this.csrService = csrService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsrResponse>>> search(
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
        var result = csrService.search(new CsrSearchCriteria(docNo, company, store, supplierCode, supplierContract, internalSupplierStore, status, createdBy, referenceNo, reasonCode, itemCode, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CsrResponse>> create(@Valid @RequestBody CsrRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSR created", csrService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CsrUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrService.update(id, request)));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<CsrResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrService.release(id)));
    }

    @PutMapping("/{id}/detail/{detailId}/actual-qty")
    public ResponseEntity<ApiResponse<CsrResponse>> updateActualQty(
            @PathVariable String id, @PathVariable String detailId,
            @Valid @RequestBody CsrActualQtyUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrService.updateActualQty(id, detailId, request)));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<CsrResponse>> complete(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrService.complete(id)));
    }
}
