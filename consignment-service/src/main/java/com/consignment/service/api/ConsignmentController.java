package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.ConsignmentRequest;
import com.consignment.service.model.ConsignmentResponse;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.UpdateConsignmentStatusRequest;
import com.consignment.service.service.ConsignmentDomainService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consignments")
public class ConsignmentController {

    private final ConsignmentDomainService consignmentDomainService;

    public ConsignmentController(ConsignmentDomainService consignmentDomainService) {
        this.consignmentDomainService = consignmentDomainService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ConsignmentResponse>> requestConsignment(
            @Valid @RequestBody ConsignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consignment request created", consignmentDomainService.createRequest(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsignmentResponse>>> listConsignments() {
        List<ConsignmentResponse> data = consignmentDomainService.listAll();
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<ConsignmentResponse>> getConsignment(@PathVariable String requestId) {
        return ResponseEntity.ok(ApiResponse.success(consignmentDomainService.getById(requestId)));
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<ApiResponse<ConsignmentResponse>> updateStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateConsignmentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(consignmentDomainService.updateStatus(requestId, request)));
    }
}
