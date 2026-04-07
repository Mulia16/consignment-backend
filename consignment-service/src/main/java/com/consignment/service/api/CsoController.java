package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.model.cso.CsoResponse;
import com.consignment.service.model.cso.CsoSearchCriteria;
import com.consignment.service.service.CsoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CsoController {

    private final CsoService csoService;

    public CsoController(CsoService csoService) {
        this.csoService = csoService;
    }

    @GetMapping("/api/cso")
    public ResponseEntity<ApiResponse<List<CsoResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csoService.search(new CsoSearchCriteria(docNo, company, store, customerCode, supplierCode, supplierContract, createdMethod, referenceNo, itemCode, status, createdBy, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @PostMapping("/api/cso")
    public ResponseEntity<ApiResponse<CsoResponse>> create(@Valid @RequestBody CsoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSO created", csoService.create(request)));
    }

    @GetMapping("/api/cso/{id}")
    public ResponseEntity<ApiResponse<CsoResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csoService.getById(id)));
    }

    @PutMapping("/api/cso/{id}/release")
    public ResponseEntity<ApiResponse<CsoResponse>> release(
            @PathVariable String id,
            @RequestHeader(value = "X-User", required = false) String releasedBy) {
        return ResponseEntity.ok(ApiResponse.success(csoService.release(id, releasedBy)));
    }

    @DeleteMapping("/api/cso/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        csoService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("CSO deleted", null));
    }

    @PostMapping("/api/acmm/cso/auto-create")
    public ResponseEntity<ApiResponse<CsoResponse>> autoCreate(@Valid @RequestBody CsoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSO auto-created", csoService.autoCreate(request)));
    }
}
