package com.consignment.service.excel;

import com.consignment.service.model.billing.CustomerBillingSearchCriteria;
import com.consignment.service.model.billing.SupplierBillingSearchCriteria;
import com.consignment.service.model.report.CustomerInventoryRow;
import com.consignment.service.model.report.ReportRow;
import com.consignment.service.model.report.StockSummaryRow;
import com.consignment.service.persistence.mapper.CustomerBillingMapper;
import com.consignment.service.persistence.mapper.SupplierBillingMapper;
import com.consignment.service.persistence.model.CustomerBillingRequestDetailEntity;
import com.consignment.service.persistence.model.CustomerBillingRequestEntity;
import com.consignment.service.persistence.model.SupplierBillingRequestDetailEntity;
import com.consignment.service.persistence.model.SupplierBillingRequestEntity;
import com.consignment.service.service.ReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportExcelService {

    private final ReportService reportService;
    private final SupplierBillingMapper supplierBillingMapper;
    private final CustomerBillingMapper customerBillingMapper;

    public ReportExcelService(ReportService reportService,
                               SupplierBillingMapper supplierBillingMapper,
                               CustomerBillingMapper customerBillingMapper) {
        this.reportService = reportService;
        this.supplierBillingMapper = supplierBillingMapper;
        this.customerBillingMapper = customerBillingMapper;
    }

    // ── Transaction reports (CSRQ, CSRV, CSO, CSDO, CSR, CSA) ──────────────

    public byte[] exportTransactionReport(String type, String company, String store,
                                          String supplierCode, String customerCode,
                                          LocalDate fromDate, LocalDate toDate, String status) {
        List<ReportRow> rows = switch (type.toUpperCase()) {
            case "CSRQ" -> reportService.csrqReport(company, store, supplierCode, fromDate, toDate, status);
            case "CSRV" -> reportService.csrvReport(company, store, supplierCode, fromDate, toDate, status);
            case "CSO"  -> reportService.csoReport(company, store, customerCode, fromDate, toDate, status);
            case "CSDO" -> reportService.csdoReport(company, store, customerCode, fromDate, toDate, status);
            case "CSR"  -> reportService.csrReport(company, store, supplierCode, fromDate, toDate, status);
            case "CSA"  -> reportService.csaReport(company, store, supplierCode, fromDate, toDate, status);
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        };

        List<String> headers = List.of("Doc No", "Type", "Company", "Store", "Supplier Code",
                "Contract", "Customer Code", "Item Code", "Qty", "UOM", "Unit Price",
                "Line Amount", "Status", "Reference No", "Business Date", "Created At");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(type + " Report");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, type + " Transaction Report", headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Period: " + fmt(fromDate) + " - " + fmt(toDate)
                    + "  |  Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            for (ReportRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.docNo()), nvl(r.documentType()), nvl(r.company()), nvl(r.store()),
                        nvl(r.supplierCode()), nvl(r.supplierContract()), nvl(r.customerCode()),
                        nvl(r.itemCode()), orZero(r.qty()), nvl(r.uom()),
                        orZero(r.unitPrice()), orZero(r.lineAmount()),
                        nvl(r.status()), nvl(r.referenceNo()),
                        r.businessDate() != null ? r.businessDate() : "-",
                        r.createdAt() != null ? r.createdAt().toString() : "-"
                ), altStyle, rowNum % 2 == 1);
                rowNum++;
            }
            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export " + type + " report", e);
        }
    }

    // ── Supplier Book Value ──────────────────────────────────────────────────

    public byte[] exportSupplierBookValue(String company, String store,
                                          String supplierCode, String supplierContract) {
        List<StockSummaryRow> rows = reportService.supplierBookValueReport(company, store, supplierCode, supplierContract);
        List<String> headers = List.of("Store", "Supplier Code", "Contract", "Item Code",
                "Purchase Qty", "Closing Qty", "Unbill Qty");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Supplier Book Value");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);
            CellStyle totalStyle = ExcelHelper.totalStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Supplier Book Value Inventory", headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            double totalPurchase = 0, totalClosing = 0, totalUnbill = 0;
            for (StockSummaryRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.store()), nvl(r.supplierCode()), nvl(r.supplierContract()),
                        nvl(r.itemCode()), orZero(r.purchaseQty()), orZero(r.closingQty()), orZero(r.unbillQty())
                ), altStyle, rowNum % 2 == 1);
                totalPurchase += r.purchaseQty() != null ? r.purchaseQty().doubleValue() : 0;
                totalClosing  += r.closingQty()  != null ? r.closingQty().doubleValue()  : 0;
                totalUnbill   += r.unbillQty()   != null ? r.unbillQty().doubleValue()   : 0;
                rowNum++;
            }
            // Total row
            Row total = sheet.createRow(rowNum);
            Cell tc = total.createCell(0);
            tc.setCellValue("TOTAL");
            tc.setCellStyle(totalStyle);
            for (int i = 1; i <= 3; i++) { Cell c = total.createCell(i); c.setCellValue(""); c.setCellStyle(totalStyle); }
            Cell tp = total.createCell(4); tp.setCellValue(totalPurchase); tp.setCellStyle(totalStyle);
            Cell tc2 = total.createCell(5); tc2.setCellValue(totalClosing); tc2.setCellStyle(totalStyle);
            Cell tu = total.createCell(6); tu.setCellValue(totalUnbill); tu.setCellStyle(totalStyle);

            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export supplier book value", e);
        }
    }

    // ── Customer Inventory ───────────────────────────────────────────────────

    public byte[] exportCustomerInventory(String store, String customerCode) {
        List<CustomerInventoryRow> rows = reportService.customerInventoryReport(store, customerCode);
        List<String> headers = List.of("Issue From Store", "Customer Code", "Branch Code", "Item Code", "Qty");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Customer Inventory");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Customer Consignment Inventory", headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            for (CustomerInventoryRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.issueFromStore()), nvl(r.customerCode()),
                        nvl(r.branchCode()), nvl(r.itemCode()), orZero(r.qty())
                ), altStyle, rowNum % 2 == 1);
                rowNum++;
            }
            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export customer inventory", e);
        }
    }

    // ── Settlement reports ───────────────────────────────────────────────────

    public byte[] exportSettlementSummary(String company, String store, String settlementType,
                                          LocalDate fromDate, LocalDate toDate, String status) {
        List<ReportRow> rows = reportService.settlementSummaryReport(company, store, settlementType, fromDate, toDate, status);
        List<String> headers = List.of("Doc No", "Company", "Store", "Settlement Type",
                "Customer Code", "Supplier Code", "Item Code", "Qty", "Unit Price",
                "Line Amount", "Status", "Business Date");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Settlement Summary");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Settlement Summary Report", headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Period: " + fmt(fromDate) + " - " + fmt(toDate)
                    + "  |  Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            for (ReportRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.docNo()), nvl(r.company()), nvl(r.store()), nvl(r.documentType()),
                        nvl(r.customerCode()), nvl(r.supplierCode()), nvl(r.itemCode()),
                        orZero(r.qty()), orZero(r.unitPrice()), orZero(r.lineAmount()),
                        nvl(r.status()), r.businessDate() != null ? r.businessDate() : "-"
                ), altStyle, rowNum % 2 == 1);
                rowNum++;
            }
            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export settlement summary", e);
        }
    }

    public byte[] exportSettlementDetail(String settlementId) {
        List<ReportRow> rows = reportService.settlementDetailReport(settlementId);
        List<String> headers = List.of("Doc No", "Type", "Item Code", "Qty", "UOM",
                "Unit Price", "Line Amount", "Status");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Settlement Detail");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Settlement Detail - " + settlementId, headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            for (ReportRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.docNo()), nvl(r.documentType()), nvl(r.itemCode()),
                        orZero(r.qty()), nvl(r.uom()), orZero(r.unitPrice()),
                        orZero(r.lineAmount()), nvl(r.status())
                ), altStyle, rowNum % 2 == 1);
                rowNum++;
            }
            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export settlement detail", e);
        }
    }

    // ── Consignment Setup ────────────────────────────────────────────────────

    public byte[] exportConsignmentSetup(String company, String store, String supplierCode) {
        List<ReportRow> rows = reportService.consignmentSetupReport(company, store, supplierCode);
        List<String> headers = List.of("Item Code", "Company", "Store", "Supplier Code",
                "Contract", "UOM/Type", "Status", "Created At");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Item Store Supplier");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Item Store Supplier Report", headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            for (ReportRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.itemCode()), nvl(r.company()), nvl(r.store()),
                        nvl(r.supplierCode()), nvl(r.supplierContract()),
                        nvl(r.uom()), nvl(r.status()),
                        r.createdAt() != null ? r.createdAt().toString() : "-"
                ), altStyle, rowNum % 2 == 1);
                rowNum++;
            }
            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export consignment setup", e);
        }
    }

    // ── Reservations ─────────────────────────────────────────────────────────

    public byte[] exportReservations(String store, String itemCode) {
        List<ReportRow> rows = reportService.reservationReport(store, itemCode);
        List<String> headers = List.of("Doc No", "Type", "Store", "Item Code", "Qty", "UOM", "Business Date");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Reservations");
            CellStyle titleStyle = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle = ExcelHelper.altRowStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Consignment Reservations Report", headers.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, headers, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            for (ReportRow r : rows) {
                ExcelHelper.addDataRow(sheet, rowNum, List.of(
                        nvl(r.docNo()), nvl(r.documentType()), nvl(r.store()),
                        nvl(r.itemCode()), orZero(r.qty()), nvl(r.uom()),
                        r.businessDate() != null ? r.businessDate() : "-"
                ), altStyle, rowNum % 2 == 1);
                rowNum++;
            }
            ExcelHelper.autoSizeColumns(sheet, headers.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export reservations", e);
        }
    }

    // ── Supplier Billing Export ───────────────────────────────────────────────

    public byte[] exportSupplierBilling(String company, String store, String supplierCode,
                                        String supplierContract, String status,
                                        LocalDate fromDate, LocalDate toDate) {
        var criteria = new SupplierBillingSearchCriteria(
                null, company, store, supplierCode, supplierContract,
                null, status, null, fromDate, toDate, 1, Integer.MAX_VALUE);
        List<SupplierBillingRequestEntity> headers = supplierBillingMapper.search(criteria);

        List<String> cols = List.of("Doc No", "Company", "Store", "Supplier Code", "Contract",
                "Period Type", "From Date", "To Date", "Status",
                "Item Code", "UOM", "Sales Qty", "Return Qty", "BF Qty",
                "Billing Qty", "CF Qty", "Unit Cost", "Amount");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Supplier Billing");
            CellStyle titleStyle  = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle    = ExcelHelper.altRowStyle(wb);
            CellStyle totalStyle  = ExcelHelper.totalStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Supplier Consignment Billing Report", cols.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Period: " + fmt(fromDate) + " - " + fmt(toDate)
                    + "  |  Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, cols, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            double grandTotal = 0;
            for (SupplierBillingRequestEntity h : headers) {
                List<SupplierBillingRequestDetailEntity> details =
                        supplierBillingMapper.findDetailsByBillingId(h.getId());
                for (SupplierBillingRequestDetailEntity d : details) {
                    double amount = d.getTotalCost() != null ? d.getTotalCost().doubleValue() : 0;
                    grandTotal += amount;
                    ExcelHelper.addDataRow(sheet, rowNum, List.of(
                            nvl(h.getDocNo()), nvl(h.getCompany()), nvl(h.getStore()),
                            nvl(h.getSupplierCode()), nvl(h.getSupplierContract()),
                            nvl(h.getPeriodType()),
                            h.getFromDate() != null ? h.getFromDate() : "-",
                            h.getToDate()   != null ? h.getToDate()   : "-",
                            nvl(h.getStatus()),
                            nvl(d.getItemCode()), nvl(d.getUom()),
                            orZero(d.getSalesQty()), orZero(d.getSalesReturnQty()),
                            orZero(d.getBfQty()), orZero(d.getBillingQty()),
                            orZero(d.getCfQty()), orZero(d.getUnitCost()), amount
                    ), altStyle, rowNum % 2 == 1);
                    rowNum++;
                }
            }
            // Total row
            Row total = sheet.createRow(rowNum);
            Cell tc = total.createCell(0); tc.setCellValue("GRAND TOTAL"); tc.setCellStyle(totalStyle);
            for (int i = 1; i < cols.size() - 1; i++) { Cell c = total.createCell(i); c.setCellValue(""); c.setCellStyle(totalStyle); }
            Cell ta = total.createCell(cols.size() - 1); ta.setCellValue(grandTotal); ta.setCellStyle(totalStyle);

            ExcelHelper.autoSizeColumns(sheet, cols.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export supplier billing", e);
        }
    }

    // ── Customer Billing Export ───────────────────────────────────────────────

    public byte[] exportCustomerBilling(String company, String store, String customerCode,
                                        String status, LocalDate fromDate, LocalDate toDate) {
        var criteria = new CustomerBillingSearchCriteria(
                null, company, store, customerCode, null,
                null, status, null, fromDate, toDate, 1, Integer.MAX_VALUE);
        List<CustomerBillingRequestEntity> headers = customerBillingMapper.search(criteria);

        List<String> cols = List.of("Doc No", "Company", "Store", "Customer Code", "Branch",
                "Period Type", "From Date", "To Date", "Status",
                "Item Code", "UOM", "Sales Qty", "Return Qty", "Billing Qty",
                "Unit Price", "Amount");

        try (XSSFWorkbook wb = ExcelHelper.newWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Customer Billing");
            CellStyle titleStyle  = ExcelHelper.titleStyle(wb);
            CellStyle headerStyle = ExcelHelper.headerStyle(wb);
            CellStyle altStyle    = ExcelHelper.altRowStyle(wb);
            CellStyle totalStyle  = ExcelHelper.totalStyle(wb);

            ExcelHelper.addTitleRow(sheet, 0, "Customer Consignment Billing Report", cols.size(), titleStyle);
            ExcelHelper.addMetaRow(sheet, 1, "Period: " + fmt(fromDate) + " - " + fmt(toDate)
                    + "  |  Generated: " + ExcelHelper.today());
            sheet.createRow(2);
            ExcelHelper.addHeaderRow(sheet, 3, cols, headerStyle);
            sheet.createFreezePane(0, 4);

            int rowNum = 4;
            double grandTotal = 0;
            for (CustomerBillingRequestEntity h : headers) {
                List<CustomerBillingRequestDetailEntity> details =
                        customerBillingMapper.findDetailsByBillingId(h.getId());
                for (CustomerBillingRequestDetailEntity d : details) {
                    double amount = d.getLineAmount() != null ? d.getLineAmount().doubleValue() : 0;
                    grandTotal += amount;
                    ExcelHelper.addDataRow(sheet, rowNum, List.of(
                            nvl(h.getDocNo()), nvl(h.getCompany()), nvl(h.getStore()),
                            nvl(h.getCustomerCode()), nvl(h.getCustomerBranch()),
                            nvl(h.getPeriodType()),
                            h.getFromDate() != null ? h.getFromDate() : "-",
                            h.getToDate()   != null ? h.getToDate()   : "-",
                            nvl(h.getStatus()),
                            nvl(d.getItemCode()), nvl(d.getUom()),
                            orZero(d.getSalesQty()), orZero(d.getReturnQty()),
                            orZero(d.getBillingQty()), orZero(d.getUnitPrice()), amount
                    ), altStyle, rowNum % 2 == 1);
                    rowNum++;
                }
            }
            // Total row
            Row total = sheet.createRow(rowNum);
            Cell tc = total.createCell(0); tc.setCellValue("GRAND TOTAL"); tc.setCellStyle(totalStyle);
            for (int i = 1; i < cols.size() - 1; i++) { Cell c = total.createCell(i); c.setCellValue(""); c.setCellStyle(totalStyle); }
            Cell ta = total.createCell(cols.size() - 1); ta.setCellValue(grandTotal); ta.setCellStyle(totalStyle);

            ExcelHelper.autoSizeColumns(sheet, cols.size());
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export customer billing", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String nvl(String v) { return v == null ? "-" : v; }
    private static double orZero(java.math.BigDecimal v) { return v == null ? 0.0 : v.doubleValue(); }
    private static String fmt(LocalDate d) { return d != null ? d.format(ExcelHelper.DATE_FMT) : "-"; }
}
