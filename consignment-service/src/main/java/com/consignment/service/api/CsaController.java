package com.consignment.service.api;

import com.consignment.service.model.csa.CsaRequest;
import com.consignment.service.model.csa.CsaResponse;
import com.consignment.service.model.csa.CsaSearchCriteria;
import com.consignment.service.service.CsaService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/csa")
public class CsaController {

    private final CsaService csaService;

    public CsaController(CsaService csaService) {
        this.csaService = csaService;
    }

    @GetMapping
    public List<CsaResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String referenceNo
    ) {
        return csaService.search(new CsaSearchCriteria(
                company,
                store,
                transactionType,
                status,
                createdBy,
                referenceNo
        ));
    }

    @GetMapping("/{id}")
    public CsaResponse getById(@PathVariable String id) {
        return csaService.getById(id);
    }

    @PostMapping
    public CsaResponse create(@Valid @RequestBody CsaRequest request) {
        return csaService.create(request);
    }

    @PutMapping("/{id}/release")
    public CsaResponse release(
            @PathVariable String id,
            @RequestHeader(value = "X-User", required = false) String releasedBy
    ) {
        return csaService.release(id, releasedBy);
    }
}
