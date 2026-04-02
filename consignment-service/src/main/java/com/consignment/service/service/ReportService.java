package com.consignment.service.service;

import com.consignment.service.model.report.CustomerInventoryRow;
import com.consignment.service.model.report.ReportRow;
import com.consignment.service.model.report.StockSummaryRow;
import com.consignment.service.persistence.mapper.ReportMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final ReportMapper reportMapper;

    public ReportService(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    public List<ReportRow> csrqReport(String company, String store, String supplierCode,
                                      LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportCsrq(n(company), n(store), n(supplierCode), fromDate, toDate, n(status));
    }

    public List<ReportRow> csrvReport(String company, String store, String supplierCode,
                                      LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportCsrv(n(company), n(store), n(supplierCode), fromDate, toDate, n(status));
    }

    public List<ReportRow> csoReport(String company, String store, String customerCode,
                                     LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportCso(n(company), n(store), n(customerCode), fromDate, toDate, n(status));
    }

    public List<ReportRow> csdoReport(String company, String store, String customerCode,
                                      LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportCsdo(n(company), n(store), n(customerCode), fromDate, toDate, n(status));
    }

    public List<ReportRow> csrReport(String company, String store, String supplierCode,
                                     LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportCsr(n(company), n(store), n(supplierCode), fromDate, toDate, n(status));
    }

    public List<ReportRow> csaReport(String company, String store, String supplierCode,
                                     LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportCsa(n(company), n(store), n(supplierCode), fromDate, toDate, n(status));
    }

    public List<ReportRow> settlementSummaryReport(String company, String store, String settlementType,
                                                   LocalDate fromDate, LocalDate toDate, String status) {
        return reportMapper.reportSettlementSummary(n(company), n(store), n(settlementType), fromDate, toDate, n(status));
    }

    public List<ReportRow> settlementDetailReport(String settlementId) {
        return reportMapper.reportSettlementDetail(settlementId);
    }

    public List<StockSummaryRow> supplierBookValueReport(String company, String store,
                                                         String supplierCode, String supplierContract) {
        return reportMapper.reportSupplierBookValue(n(company), n(store), n(supplierCode), n(supplierContract));
    }

    public List<CustomerInventoryRow> customerInventoryReport(String store, String customerCode) {
        return reportMapper.reportCustomerInventory(n(store), n(customerCode));
    }

    public List<ReportRow> reservationReport(String store, String itemCode) {
        return reportMapper.reportReservation(n(store), n(itemCode));
    }

    public List<ReportRow> consignmentSetupReport(String company, String store, String supplierCode) {
        return reportMapper.reportConsignmentSetup(n(company), n(store), n(supplierCode));
    }

    private String n(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
