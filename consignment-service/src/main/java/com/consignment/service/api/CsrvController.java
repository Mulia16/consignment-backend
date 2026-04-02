package com.consignment.service.api;

import com.consignment.service.model.csrv.CsrvRequest;
import com.consignment.service.model.csrv.CsrvResponse;
import com.consignment.service.model.csrv.CsrvSearchCriteria;
import com.consignment.service.service.CsrvService;
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
@RequestMapping
public class CsrvController {

    private final CsrvService csrvService;

    public CsrvController(CsrvService csrvService) {
        this.csrvService = csrvService;
    }

    @GetMapping("/api/csrv")
    public List<CsrvResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String receivingStore,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status
    ) {
        return csrvService.search(new CsrvSearchCriteria(
                company,
                receivingStore,
                supplierCode,
                supplierContract,
                branch,
                createdMethod,
                referenceNo,
                itemCode,
                status
        ));
    }

    @PostMapping("/api/csrv")
    public CsrvResponse create(@Valid @RequestBody CsrvRequest request) {
        return csrvService.create(request);
    }

    @GetMapping("/api/csrv/{id}")
    public CsrvResponse getById(@PathVariable String id) {
        return csrvService.getById(id);
    }

    @PutMapping("/api/csrv/{id}/release")
    public CsrvResponse release(@PathVariable String id) {
        return csrvService.release(id);
    }

    @PostMapping("/api/acmm/csrv/auto-create")
    public CsrvResponse autoCreate(@Valid @RequestBody CsrvRequest request) {
        return csrvService.autoCreate(request);
    }
}
