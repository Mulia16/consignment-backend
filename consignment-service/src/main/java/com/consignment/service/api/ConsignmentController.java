package com.consignment.service.api;

import com.consignment.service.model.ConsignmentRequest;
import com.consignment.service.model.ConsignmentResponse;
import com.consignment.service.model.UpdateConsignmentStatusRequest;
import com.consignment.service.service.ConsignmentDomainService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consignments")
public class ConsignmentController {

    private final ConsignmentDomainService consignmentDomainService;

    public ConsignmentController(ConsignmentDomainService consignmentDomainService) {
        this.consignmentDomainService = consignmentDomainService;
    }

    @PostMapping("/request")
    public ConsignmentResponse requestConsignment(@Valid @RequestBody ConsignmentRequest request) {
        return consignmentDomainService.createRequest(request);
    }

    @GetMapping
    public List<ConsignmentResponse> listConsignments() {
        return consignmentDomainService.listAll();
    }

    @GetMapping("/{requestId}")
    public ConsignmentResponse getConsignment(@PathVariable String requestId) {
        return consignmentDomainService.getById(requestId);
    }

    @PatchMapping("/{requestId}/status")
    public ConsignmentResponse updateStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateConsignmentStatusRequest request
    ) {
        return consignmentDomainService.updateStatus(requestId, request);
    }
}
