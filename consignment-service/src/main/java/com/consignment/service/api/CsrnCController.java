package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csrn.CsrnCActualQtyRequest;
import com.consignment.service.model.csrn.CsrnCResponse;
import com.consignment.service.model.csrn.CsrnCSearchCriteria;
import com.consignment.service.service.CsrnCService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/csrn-c")
public class CsrnCController {

    private final CsrnCService csrnCService;

    public CsrnCController(CsrnCService csrnCService) {
        this.csrnCService = csrnCService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CsrnCResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String reasonCode,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String csrnDocNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csrnCService.search(new CsrnCSearchCriteria(
                docNo, company, store, supplierCode, supplierContract, reasonCode,
                createdBy, csrnDocNo, status, createdFrom, createdTo, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrnCResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnCService.getById(id)));
    }

    /** Update actual return qty — only allowed when status is HELD */
    @PutMapping("/{id}/detail/{detailId}/actual-qty")
    public ResponseEntity<ApiResponse<CsrnCResponse>> updateActualQty(
            @PathVariable String id,
            @PathVariable String detailId,
            @Valid @RequestBody CsrnCActualQtyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(csrnCService.updateActualQty(id, detailId, request)));
    }

    /** Complete CSRN-C — posts to inventory and sets status to UPDATED */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<CsrnCResponse>> complete(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnCService.complete(id)));
    }
}
