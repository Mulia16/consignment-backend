package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csrn.*;
import com.consignment.service.service.CsrnService;
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
@RequestMapping("/api/csrn")
public class CsrnController {

    private final CsrnService csrnService;
    private final CsrnSlipService csrnSlipService;

    public CsrnController(CsrnService csrnService, CsrnSlipService csrnSlipService) {
        this.csrnService = csrnService;
        this.csrnSlipService = csrnSlipService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsrnResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String csoDocNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csrnService.search(new CsrnSearchCriteria(
                docNo, company, store, supplierCode, csoDocNo, status, createdBy, itemCode,
                updatedFrom, updatedTo, page, perPage));
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

    /** Update CSRN (only HELD). Auto-creates CSRN-C snapshot and sets status to UPDATED */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrnResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CsrnUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.update(id, request)));
    }

    /** Get CSRN-C (auto-created snapshot) for a given CSRN */
    @GetMapping("/{id}/csrn-c")
    public ResponseEntity<ApiResponse<CsrnCResponse>> getCsrnC(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnService.getCsrnC(id)));
    }

    /** Print slip PDF — only available for UPDATED status */
    @GetMapping("/{id}/slip")
    public ResponseEntity<byte[]> printSlip(@PathVariable String id) {
        byte[] pdf = csrnSlipService.generateSlip(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"CSRN-slip-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
