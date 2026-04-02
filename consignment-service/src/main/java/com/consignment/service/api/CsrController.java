package com.consignment.service.api;

import com.consignment.service.model.csr.CsrActualQtyUpdateRequest;
import com.consignment.service.model.csr.CsrRequest;
import com.consignment.service.model.csr.CsrResponse;
import com.consignment.service.model.csr.CsrSearchCriteria;
import com.consignment.service.service.CsrService;
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
@RequestMapping("/api/csr")
public class CsrController {

    private final CsrService csrService;

    public CsrController(CsrService csrService) {
        this.csrService = csrService;
    }

    @GetMapping
    public List<CsrResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode
    ) {
        return csrService.search(new CsrSearchCriteria(
                company,
                store,
                supplierCode,
                supplierContract,
                status,
                createdBy,
                referenceNo,
                itemCode
        ));
    }

    @GetMapping("/{id}")
    public CsrResponse getById(@PathVariable String id) {
        return csrService.getById(id);
    }

    @PostMapping
    public CsrResponse create(@Valid @RequestBody CsrRequest request) {
        return csrService.create(request);
    }

    @PutMapping("/{id}/release")
    public CsrResponse release(@PathVariable String id) {
        return csrService.release(id);
    }

    @PutMapping("/{id}/detail/{detailId}/actual-qty")
    public CsrResponse updateActualQty(
            @PathVariable String id,
            @PathVariable String detailId,
            @Valid @RequestBody CsrActualQtyUpdateRequest request
    ) {
        return csrService.updateActualQty(id, detailId, request);
    }

    @PutMapping("/{id}/complete")
    public CsrResponse complete(@PathVariable String id) {
        return csrService.complete(id);
    }
}
