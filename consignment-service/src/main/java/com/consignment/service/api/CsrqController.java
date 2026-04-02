package com.consignment.service.api;

import com.consignment.service.model.csrq.CsrqRequest;
import com.consignment.service.model.csrq.CsrqResponse;
import com.consignment.service.model.csrq.CsrqSearchCriteria;
import com.consignment.service.service.CsrqService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/csrq")
public class CsrqController {

    private final CsrqService csrqService;

    public CsrqController(CsrqService csrqService) {
        this.csrqService = csrqService;
    }

    @GetMapping
    public List<CsrqResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String internalSupplierStore,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status
    ) {
        return csrqService.search(new CsrqSearchCriteria(
                company,
                store,
                supplierCode,
                supplierContract,
                branch,
                internalSupplierStore,
                createdMethod,
                referenceNo,
                itemCode,
                status
        ));
    }

    @PostMapping
    public CsrqResponse create(@Valid @RequestBody CsrqRequest request) {
        return csrqService.create(request);
    }

    @GetMapping("/{id}")
    public CsrqResponse getById(@PathVariable String id) {
        return csrqService.getById(id);
    }

    @PutMapping("/{id}/release")
    public CsrqResponse release(@PathVariable String id) {
        return csrqService.release(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        csrqService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
