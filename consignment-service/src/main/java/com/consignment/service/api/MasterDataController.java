package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides master data lookups derived from consignment setup.
 * Used to populate dropdowns in forms (company, store, supplier, contract, item).
 */
@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {

    private final ConsignmentSetupMapper setupMapper;

    public MasterDataController(ConsignmentSetupMapper setupMapper) {
        this.setupMapper = setupMapper;
    }

    /** All registered companies */
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<String>>> companies() {
        return ResponseEntity.ok(ApiResponse.success(setupMapper.findDistinctCompanies()));
    }

    /** Stores available for a given company (or all stores if company omitted) */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<List<String>>> stores(
            @RequestParam(required = false) String company) {
        return ResponseEntity.ok(ApiResponse.success(setupMapper.findStoresByCompany(company)));
    }

    /** Supplier codes available for a given company+store */
    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<List<String>>> suppliers(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store) {
        return ResponseEntity.ok(ApiResponse.success(setupMapper.findSuppliersByStore(company, store)));
    }

    /** Supplier contracts available for a given company+store+supplier */
    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<List<String>>> contracts(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode) {
        return ResponseEntity.ok(ApiResponse.success(
                setupMapper.findContractsBySupplier(company, store, supplierCode)));
    }

    /** Item codes registered under a given company+store+supplier+contract */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<String>>> items(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract) {
        return ResponseEntity.ok(ApiResponse.success(
                setupMapper.findItemsBySupplierContract(company, store, supplierCode, supplierContract)));
    }
}
