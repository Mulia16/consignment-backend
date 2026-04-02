package com.consignment.service.persistence.mapper;

import com.consignment.service.model.report.CustomerInventoryRow;
import com.consignment.service.model.report.ReportRow;
import com.consignment.service.model.report.StockSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {

    // R01 - CSRQ list by period
    List<ReportRow> reportCsrq(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R02 - CSRV list by period
    List<ReportRow> reportCsrv(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R03 - CSO list by period
    List<ReportRow> reportCso(
            @Param("company") String company,
            @Param("store") String store,
            @Param("customerCode") String customerCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R04 - CSDO list by period
    List<ReportRow> reportCsdo(
            @Param("company") String company,
            @Param("store") String store,
            @Param("customerCode") String customerCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R05 - CSR list by period
    List<ReportRow> reportCsr(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R06 - CSA list by period
    List<ReportRow> reportCsa(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R07 - Settlement summary by period
    List<ReportRow> reportSettlementSummary(
            @Param("company") String company,
            @Param("store") String store,
            @Param("settlementType") String settlementType,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // R08 - Settlement detail lines by settlement ID
    List<ReportRow> reportSettlementDetail(
            @Param("settlementId") String settlementId
    );

    // R09 - Supplier book value (stock on hand at supplier BV)
    List<StockSummaryRow> reportSupplierBookValue(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract
    );

    // R10 - Customer consignment inventory on hand
    List<CustomerInventoryRow> reportCustomerInventory(
            @Param("store") String store,
            @Param("customerCode") String customerCode
    );

    // R11 - Reservation summary (open allocations + forecasts)
    List<ReportRow> reportReservation(
            @Param("store") String store,
            @Param("itemCode") String itemCode
    );

    // R12 - Consignment setup item-supplier mapping
    List<ReportRow> reportConsignmentSetup(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode
    );
}
