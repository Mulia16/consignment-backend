package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csa.CsaRequest;
import com.consignment.service.model.csa.CsaResponse;
import com.consignment.service.model.csa.CsaSearchCriteria;
import com.consignment.service.pdf.CsaSlipService;
import com.consignment.service.service.CsaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csa")
public class CsaController {

    private final CsaService csaService;
    private final CsaSlipService csaSlipService;

    public CsaController(CsaService csaService, CsaSlipService csaSlipService) {
        this.csaService = csaService;
        this.csaSlipService = csaSlipService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsaResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String reasonCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csaService.search(new CsaSearchCriteria(docNo, company, store, supplierCode, supplierContract, transactionType, status, createdBy, referenceNo, reasonCode, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsaResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csaService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CsaResponse>> create(@Valid @RequestBody CsaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSA created", csaService.create(request)));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<CsaResponse>> release(
            @PathVariable String id,
            @RequestHeader(value = "X-User", required = false) String releasedBy) {
        return ResponseEntity.ok(ApiResponse.success(csaService.release(id, releasedBy)));
    }

    @GetMapping("/{id}/slip")
    public ResponseEntity<byte[]> printSlip(@PathVariable String id) {
        byte[] pdf = csaSlipService.generate(id);
        String filename = "CSA-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Filename", filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
