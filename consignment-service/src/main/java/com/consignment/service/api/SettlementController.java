package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import com.consignment.service.model.settlement.SettlementDocumentPostRequest;
import com.consignment.service.model.settlement.SettlementRequest;
import com.consignment.service.model.settlement.SettlementResponse;
import com.consignment.service.model.settlement.SettlementSearchCriteria;
import com.consignment.service.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> search(
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String settlementType,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var result = settlementService.search(new SettlementSearchCriteria(docNo, company, store, settlementType, customerCode, supplierCode, supplierContract, status, createdBy, referenceNo, page, perPage));
        return ResponseEntity.ok(ApiResponse.paginated(result.items(), result.meta()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SettlementResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SettlementResponse>> create(@Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement created", settlementService.create(request)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SettlementResponse>> generate(@Valid @RequestBody SettlementBatchGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement generated", settlementService.generateBatch(request)));
    }

    @PostMapping("/{id}/details/from-documents")
    public ResponseEntity<ApiResponse<SettlementResponse>> postDetailsFromDocuments(
            @PathVariable String id, @Valid @RequestBody SettlementDocumentPostRequest request) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.postDetailsFromDocuments(id, request)));
    }

    @PutMapping("/{id}/prepare-for-billing")
    public ResponseEntity<ApiResponse<SettlementResponse>> prepareForBilling(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.prepareForBilling(id)));
    }

    @PutMapping("/{id}/mark-as-billed")
    public ResponseEntity<ApiResponse<SettlementResponse>> markAsBilled(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.markAsBilled(id)));
    }

    @PutMapping("/{id}/mark-as-settled")
    public ResponseEntity<ApiResponse<SettlementResponse>> markAsSettled(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.markAsSettled(id)));
    }
}
