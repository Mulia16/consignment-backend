package com.consignment.service.api;

import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.model.cso.CsoResponse;
import com.consignment.service.model.cso.CsoSearchCriteria;
import com.consignment.service.service.CsoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class CsoController {

    private final CsoService csoService;

    public CsoController(CsoService csoService) {
        this.csoService = csoService;
    }

    @GetMapping("/api/cso")
    public List<CsoResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status
    ) {
        return csoService.search(new CsoSearchCriteria(
                company,
                store,
                customerCode,
                supplierCode,
                supplierContract,
                createdMethod,
                referenceNo,
                itemCode,
                status
        ));
    }

    @PostMapping("/api/cso")
    public CsoResponse create(@Valid @RequestBody CsoRequest request) {
        return csoService.create(request);
    }

    @GetMapping("/api/cso/{id}")
    public CsoResponse getById(@PathVariable String id) {
        return csoService.getById(id);
    }

    @PutMapping("/api/cso/{id}/release")
    public CsoResponse release(
            @PathVariable String id,
            @RequestHeader(value = "X-User", required = false) String releasedBy
    ) {
        return csoService.release(id, releasedBy);
    }

    @DeleteMapping("/api/cso/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        csoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/acmm/cso/auto-create")
    public CsoResponse autoCreate(@Valid @RequestBody CsoRequest request) {
        return csoService.autoCreate(request);
    }
}
