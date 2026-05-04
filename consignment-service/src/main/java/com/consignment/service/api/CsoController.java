package com.consignment.service.api;

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.model.cso.CsoResponse;
import com.consignment.service.model.cso.CsoSearchCriteria;
import com.consignment.service.model.cso.CsoUpdateRequest;
import com.consignment.service.pdf.CsoSlipService;
import com.consignment.service.service.CsoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
public class CsoController {

    private final CsoService csoService;
    private final CsoSlipService csoSlipService;
    private final ConsigneeContext consigneeContext;

    public CsoController(CsoService csoService, CsoSlipService csoSlipService, ConsigneeContext consigneeContext) {
        this.csoService = csoService;
        this.csoSlipService = csoSlipService;
        this.consigneeContext = consigneeContext;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csoService.search(new CsoSearchCriteria(docNo, company, store, customerCode, supplierCode, supplierContract, createdMethod, referenceNo, itemCode, status, createdBy, dateFrom, dateTo, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @PostMapping("/api/cso")
    public ResponseEntity<ApiResponse<CsoResponse>> create(@Valid @RequestBody CsoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSO created", csoService.create(request)));
    }

    @PutMapping("/api/cso/{id}")
    public ResponseEntity<ApiResponse<CsoResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CsoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csoService.update(id, request)));
    }

    @GetMapping("/api/cso/{id}")
    public ResponseEntity<ApiResponse<CsoResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csoService.getById(id)));
    }

    @PutMapping("/api/cso/{id}/release")
    public ResponseEntity<ApiResponse<CsoResponse>> release(
            @PathVariable String id,
            @RequestHeader(value = "X-User", required = false) String releasedBy) {
        String authenticatedUser = consigneeContext.getCurrentUsername();
        String effectiveReleasedBy = authenticatedUser != null ? authenticatedUser : releasedBy;
        return ResponseEntity.ok(ApiResponse.success(csoService.release(id, effectiveReleasedBy)));
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

    @GetMapping("/api/cso/{id}/slip")
    public ResponseEntity<byte[]> printSlip(@PathVariable String id) {
        byte[] pdf = csoSlipService.generate(id);
        String filename = "CSO-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Filename", filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
