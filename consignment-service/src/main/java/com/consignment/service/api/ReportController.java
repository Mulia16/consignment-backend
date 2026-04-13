package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.report.CustomerInventoryRow;
import com.consignment.service.model.report.ReportRow;
import com.consignment.service.model.report.StockSummaryRow;
import com.consignment.service.excel.ReportExcelService;
import com.consignment.service.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExcelService reportExcelService;

    public ReportController(ReportService reportService, ReportExcelService reportExcelService) {
        this.reportService = reportService;
        this.reportExcelService = reportExcelService;
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

    // ── Excel Export Endpoints ────────────────────────────────────────────────

    @GetMapping("/{type}/export")
    public ResponseEntity<byte[]> exportTransaction(
            @PathVariable String type,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        byte[] xlsx = reportExcelService.exportTransactionReport(type, company, store, supplierCode, customerCode, fromDate, toDate, status);
        return excelResponse(xlsx, type + "-report.xlsx");
    }

    @GetMapping("/supplier-book-value/export")
    public ResponseEntity<byte[]> exportSupplierBookValue(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode, @RequestParam(required = false) String supplierContract) {
        return excelResponse(reportExcelService.exportSupplierBookValue(company, store, supplierCode, supplierContract),
                "SupplierBookValue.xlsx");
    }

    @GetMapping("/customer-inventory/export")
    public ResponseEntity<byte[]> exportCustomerInventory(
            @RequestParam(required = false) String store, @RequestParam(required = false) String customerCode) {
        return excelResponse(reportExcelService.exportCustomerInventory(store, customerCode), "CustomerInventory.xlsx");
    }

    @GetMapping("/settlement-summary/export")
    public ResponseEntity<byte[]> exportSettlementSummary(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String settlementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return excelResponse(reportExcelService.exportSettlementSummary(company, store, settlementType, fromDate, toDate, status),
                "SettlementSummary.xlsx");
    }

    @GetMapping("/settlement-detail/{settlementId}/export")
    public ResponseEntity<byte[]> exportSettlementDetail(@PathVariable String settlementId) {
        return excelResponse(reportExcelService.exportSettlementDetail(settlementId),
                "SettlementDetail-" + settlementId + ".xlsx");
    }

    @GetMapping("/consignment-setup/export")
    public ResponseEntity<byte[]> exportConsignmentSetup(
            @RequestParam(required = false) String company, @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode) {
        return excelResponse(reportExcelService.exportConsignmentSetup(company, store, supplierCode),
                "ItemStoreSupplier.xlsx");
    }

    @GetMapping("/reservations/export")
    public ResponseEntity<byte[]> exportReservations(
            @RequestParam(required = false) String store, @RequestParam(required = false) String itemCode) {
        return excelResponse(reportExcelService.exportReservations(store, itemCode), "Reservations.xlsx");
    }

    private ResponseEntity<byte[]> excelResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Filename", filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
