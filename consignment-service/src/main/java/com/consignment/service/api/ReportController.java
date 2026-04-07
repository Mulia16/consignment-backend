package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.report.CustomerInventoryRow;
import com.consignment.service.model.report.ReportRow;
import com.consignment.service.model.report.StockSummaryRow;
import com.consignment.service.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/csrq")
    public ResponseEntity<ApiResponse<List<ReportRow>>> csrq(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.csrqReport(company, store, supplierCode, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/csrv")
    public ResponseEntity<ApiResponse<List<ReportRow>>> csrv(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.csrvReport(company, store, supplierCode, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/cso")
    public ResponseEntity<ApiResponse<List<ReportRow>>> cso(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.csoReport(company, store, customerCode, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/csdo")
    public ResponseEntity<ApiResponse<List<ReportRow>>> csdo(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.csdoReport(company, store, customerCode, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/csr")
    public ResponseEntity<ApiResponse<List<ReportRow>>> csr(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.csrReport(company, store, supplierCode, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/csa")
    public ResponseEntity<ApiResponse<List<ReportRow>>> csa(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.csaReport(company, store, supplierCode, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/settlement-summary")
    public ResponseEntity<ApiResponse<List<ReportRow>>> settlementSummary(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String settlementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        List<ReportRow> data = reportService.settlementSummaryReport(company, store, settlementType, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/settlement-detail/{settlementId}")
    public ResponseEntity<ApiResponse<List<ReportRow>>> settlementDetail(@PathVariable String settlementId) {
        List<ReportRow> data = reportService.settlementDetailReport(settlementId);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/supplier-book-value")
    public ResponseEntity<ApiResponse<List<StockSummaryRow>>> supplierBookValue(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode, @RequestParam(required = false) String supplierContract) {
        List<StockSummaryRow> data = reportService.supplierBookValueReport(company, store, supplierCode, supplierContract);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/customer-inventory")
    public ResponseEntity<ApiResponse<List<CustomerInventoryRow>>> customerInventory(
            @RequestParam(required = false) String store, @RequestParam(required = false) String customerCode) {
        List<CustomerInventoryRow> data = reportService.customerInventoryReport(store, customerCode);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<List<ReportRow>>> reservations(
            @RequestParam(required = false) String store, @RequestParam(required = false) String itemCode) {
        List<ReportRow> data = reportService.reservationReport(store, itemCode);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }

    @GetMapping("/consignment-setup")
    public ResponseEntity<ApiResponse<List<ReportRow>>> consignmentSetup(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode) {
        List<ReportRow> data = reportService.consignmentSetupReport(company, store, supplierCode);
        return ResponseEntity.ok(ApiResponse.paginated(data, PageMeta.of(1, data.size(), data.size())));
    }
}
