package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csdo.CsdoResponse;
import com.consignment.service.model.csdo.CsdoSearchCriteria;
import com.consignment.service.model.csdo.CsdoTransferRequest;
import com.consignment.service.model.csdo.CsdoUpdateRequest;
import com.consignment.service.pdf.CsdoSlipService;
import com.consignment.service.service.CsdoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csdo")
public class CsdoController {

    private final CsdoService csdoService;
    private final CsdoSlipService csdoSlipService;

    public CsdoController(CsdoService csdoService, CsdoSlipService csdoSlipService) {
        this.csdoService = csdoService;
        this.csdoSlipService = csdoSlipService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsdoResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String csoDocNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String itemCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csdoService.search(new CsdoSearchCriteria(docNo, company, store, customerCode, csoDocNo, status, createdBy, itemCode, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsdoResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csdoService.getById(id)));
    }

    @PostMapping("/transfer/{csoId}")
    public ResponseEntity<ApiResponse<CsdoResponse>> transferFromCso(
            @PathVariable String csoId,
            @Valid @RequestBody CsdoTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSDO created from CSO", csdoService.transferFromCso(csoId, request)));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<CsdoResponse>> release(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csdoService.release(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CsdoResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CsdoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csdoService.update(id, request)));
    }

    @PutMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<CsdoResponse>> reverseCorrection(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csdoService.reverseCorrection(id)));
    }

    @GetMapping("/{id}/slip")
    public ResponseEntity<byte[]> printSlip(@PathVariable String id) {
        byte[] pdf = csdoSlipService.generate(id);
        String filename = "CSDO-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Filename", filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
