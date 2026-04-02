package com.consignment.service.api;

import com.consignment.service.model.setup.ConsignmentSetupItemResponse;
import com.consignment.service.model.setup.ExternalSupplierSetupRequest;
import com.consignment.service.model.setup.ExternalSupplierSetupResponse;
import com.consignment.service.model.setup.InternalSupplierSetupRequest;
import com.consignment.service.model.setup.InternalSupplierSetupResponse;
import com.consignment.service.service.ConsignmentSetupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consignment-setup")
public class ConsignmentSetupController {

    private final ConsignmentSetupService consignmentSetupService;

    public ConsignmentSetupController(ConsignmentSetupService consignmentSetupService) {
        this.consignmentSetupService = consignmentSetupService;
    }

    @GetMapping("/items")
    public List<ConsignmentSetupItemResponse> listItems() {
        return consignmentSetupService.listItems();
    }

    @GetMapping("/item/{itemCode}")
    public ConsignmentSetupItemResponse getByItemCode(@PathVariable String itemCode) {
        return consignmentSetupService.getByItemCode(itemCode);
    }

    @PostMapping("/item/{itemCode}/external-supplier")
    public ExternalSupplierSetupResponse createExternalSupplier(
            @PathVariable String itemCode,
            @Valid @RequestBody ExternalSupplierSetupRequest request
    ) {
        return consignmentSetupService.addExternalSupplier(itemCode, request);
    }

    @PutMapping("/item/{itemCode}/external-supplier/{id}")
    public ExternalSupplierSetupResponse updateExternalSupplier(
            @PathVariable String itemCode,
            @PathVariable String id,
            @Valid @RequestBody ExternalSupplierSetupRequest request
    ) {
        return consignmentSetupService.updateExternalSupplier(itemCode, id, request);
    }

    @DeleteMapping("/item/{itemCode}/external-supplier/{id}")
    public ResponseEntity<Void> deleteExternalSupplier(
            @PathVariable String itemCode,
            @PathVariable String id
    ) {
        consignmentSetupService.deleteExternalSupplier(itemCode, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/item/{itemCode}/internal-supplier")
    public InternalSupplierSetupResponse createInternalSupplier(
            @PathVariable String itemCode,
            @Valid @RequestBody InternalSupplierSetupRequest request
    ) {
        return consignmentSetupService.addInternalSupplier(itemCode, request);
    }
}
