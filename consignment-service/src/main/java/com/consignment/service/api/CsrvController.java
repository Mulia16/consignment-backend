package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csrv.CsrvRequest;
import com.consignment.service.model.csrv.CsrvResponse;
import com.consignment.service.model.csrv.CsrvSearchCriteria;
import com.consignment.service.model.csrv.CsrvUpdateRequest;
import com.consignment.service.pdf.CsrvSlipService;
import com.consignment.service.service.CsrvService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CsrvController {

    private final CsrvService csrvService;
    private final CsrvSlipService csrvSlipService;

    public CsrvController(CsrvService csrvService, CsrvSlipService csrvSlipService) {
        this.csrvService = csrvService;
        this.csrvSlipService = csrvSlipService;
    }

    @GetMapping("/api/csrv")
    public ResponseEntity<ApiResponse<List<CsrvResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String receivingStore,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String supplierDoNo,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csrvService.search(new CsrvSearchCriteria(docNo, company, receivingStore, supplierCode, supplierContract, branch, supplierDoNo, createdMethod, referenceNo, itemCode, status, createdBy, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @PostMapping("/api/csrv")
    public ResponseEntity<ApiResponse<CsrvResponse>> create(@Valid @RequestBody CsrvRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSRV created", csrvService.create(request)));
    }

    @PutMapping("/api/csrv/{id}")
    public ResponseEntity<ApiResponse<CsrvResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CsrvUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrvService.update(id, request)));
    }

    @GetMapping("/api/csrv/{id}")
    public ResponseEntity<ApiResponse<CsrvResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrvService.getById(id)));
    }

    @PutMapping("/api/csrv/{id}/release")
    public ResponseEntity<ApiResponse<CsrvResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrvService.release(id)));
    }

    @PostMapping("/api/acmm/csrv/auto-create")
    public ResponseEntity<ApiResponse<CsrvResponse>> autoCreate(@Valid @RequestBody CsrvRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSRV auto-created", csrvService.autoCreate(request)));
    }

    @GetMapping("/api/csrv/{id}/slip")
    public ResponseEntity<byte[]> printSlip(@PathVariable String id) {
        byte[] pdf = csrvSlipService.generate(id);
        String filename = "CSRV-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Filename", filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
