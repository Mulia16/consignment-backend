package com.consignment.service.api;

import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import com.consignment.service.model.settlement.SettlementDocumentPostRequest;
import com.consignment.service.model.settlement.SettlementRequest;
import com.consignment.service.model.settlement.SettlementResponse;
import com.consignment.service.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public List<SettlementResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String settlementType,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy
    ) {
        return settlementService.search(company, store, settlementType, customerCode, supplierCode, status, createdBy);
    }

    @GetMapping("/{id}")
    public SettlementResponse getById(@PathVariable String id) {
        return settlementService.getById(id);
    }

    @PostMapping
    public SettlementResponse create(@Valid @RequestBody SettlementRequest request) {
        return settlementService.create(request);
    }

    @PostMapping("/generate")
    public SettlementResponse generate(@Valid @RequestBody SettlementBatchGenerateRequest request) {
        return settlementService.generateBatch(request);
    }

    @PostMapping("/{id}/details/from-documents")
    public SettlementResponse postDetailsFromDocuments(
            @PathVariable String id,
            @Valid @RequestBody SettlementDocumentPostRequest request
    ) {
        return settlementService.postDetailsFromDocuments(id, request);
    }

    @PutMapping("/{id}/prepare-for-billing")
    public SettlementResponse prepareForBilling(@PathVariable String id) {
        return settlementService.prepareForBilling(id);
    }

    @PutMapping("/{id}/mark-as-billed")
    public SettlementResponse markAsBilled(@PathVariable String id) {
        return settlementService.markAsBilled(id);
    }

    @PutMapping("/{id}/mark-as-settled")
    public SettlementResponse markAsSettled(@PathVariable String id) {
        return settlementService.markAsSettled(id);
    }
}
