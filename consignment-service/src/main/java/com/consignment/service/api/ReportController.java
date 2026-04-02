package com.consignment.service.api;

import com.consignment.service.model.report.CustomerInventoryRow;
import com.consignment.service.model.report.ReportRow;
import com.consignment.service.model.report.StockSummaryRow;
import com.consignment.service.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
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

    /** R01 - CSRQ transactions by period */
    @GetMapping("/csrq")
    public List<ReportRow> csrq(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.csrqReport(company, store, supplierCode, fromDate, toDate, status);
    }

    /** R02 - CSRV transactions by period */
    @GetMapping("/csrv")
    public List<ReportRow> csrv(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.csrvReport(company, store, supplierCode, fromDate, toDate, status);
    }

    /** R03 - CSO transactions by period */
    @GetMapping("/cso")
    public List<ReportRow> cso(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.csoReport(company, store, customerCode, fromDate, toDate, status);
    }

    /** R04 - CSDO transactions by period */
    @GetMapping("/csdo")
    public List<ReportRow> csdo(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.csdoReport(company, store, customerCode, fromDate, toDate, status);
    }

    /** R05 - CSR transactions by period */
    @GetMapping("/csr")
    public List<ReportRow> csr(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.csrReport(company, store, supplierCode, fromDate, toDate, status);
    }

    /** R06 - CSA transactions by period */
    @GetMapping("/csa")
    public List<ReportRow> csa(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.csaReport(company, store, supplierCode, fromDate, toDate, status);
    }

    /** R07 - Settlement summary by period */
    @GetMapping("/settlement-summary")
    public List<ReportRow> settlementSummary(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String settlementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return reportService.settlementSummaryReport(company, store, settlementType, fromDate, toDate, status);
    }

    /** R08 - Settlement detail lines */
    @GetMapping("/settlement-detail/{settlementId}")
    public List<ReportRow> settlementDetail(@PathVariable String settlementId) {
        return reportService.settlementDetailReport(settlementId);
    }

    /** R09 - Supplier book value inventory */
    @GetMapping("/supplier-book-value")
    public List<StockSummaryRow> supplierBookValue(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierContract) {
        return reportService.supplierBookValueReport(company, store, supplierCode, supplierContract);
    }

    /** R10 - Customer consignment inventory on hand */
    @GetMapping("/customer-inventory")
    public List<CustomerInventoryRow> customerInventory(
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String customerCode) {
        return reportService.customerInventoryReport(store, customerCode);
    }

    /** R11 - Open reservations (allocations + forecasts) */
    @GetMapping("/reservations")
    public List<ReportRow> reservations(
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String itemCode) {
        return reportService.reservationReport(store, itemCode);
    }

    /** R12 - Consignment setup item-supplier mapping */
    @GetMapping("/consignment-setup")
    public List<ReportRow> consignmentSetup(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String supplierCode) {
        return reportService.consignmentSetupReport(company, store, supplierCode);
    }
}
