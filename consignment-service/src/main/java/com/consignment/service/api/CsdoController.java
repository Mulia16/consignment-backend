package com.consignment.service.api;

import com.consignment.service.model.csdo.CsdoResponse;
import com.consignment.service.model.csdo.CsdoSearchCriteria;
import com.consignment.service.model.csdo.CsdoTransferRequest;
import com.consignment.service.service.CsdoService;
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
@RequestMapping("/api/csdo")
public class CsdoController {

    private final CsdoService csdoService;

    public CsdoController(CsdoService csdoService) {
        this.csdoService = csdoService;
    }

    @GetMapping
    public List<CsdoResponse> search(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdMethod,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String itemCode
    ) {
        return csdoService.search(new CsdoSearchCriteria(
                company,
                store,
                customerCode,
                status,
                createdMethod,
                referenceNo,
                itemCode
        ));
    }

    @GetMapping("/{id}")
    public CsdoResponse getById(@PathVariable String id) {
        return csdoService.getById(id);
    }

    @PostMapping("/transfer/{csoId}")
    public CsdoResponse transferFromCso(
            @PathVariable String csoId,
            @Valid @RequestBody CsdoTransferRequest request
    ) {
        return csdoService.transferFromCso(csoId, request);
    }

    @PutMapping("/{id}/release")
    public CsdoResponse release(@PathVariable String id) {
        return csdoService.release(id);
    }

    @PutMapping("/{id}/reverse")
    public CsdoResponse reverseCorrection(@PathVariable String id) {
        return csdoService.reverseCorrection(id);
    }
}
