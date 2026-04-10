package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.csrn.CsrnCResponse;
import com.consignment.service.model.csrn.CsrnCSearchCriteria;
import com.consignment.service.service.CsrnCService;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = csrnCService.search(new CsrnCSearchCriteria(
                docNo, company, store, supplierCode, supplierContract, reasonCode,
                createdBy, csrnDocNo, createdFrom, createdTo, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CsrnCResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(csrnCService.getById(id)));
    }
}
