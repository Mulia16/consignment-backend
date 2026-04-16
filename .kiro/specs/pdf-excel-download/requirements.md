# Requirements: PDF & Excel Download Feature

## Overview
Implementasi fitur download PDF (print slip dokumen transaksi) dan Excel (export report)
untuk Consignment Management System.

Stack: Java Spring Boot + OpenPDF (sudah ada di pom.xml) + Apache POI (sudah ada di pom.xml)
Package PDF  : `com.consignment.service.pdf`
Package Excel: `com.consignment.service.excel`

---

## Status Existing

### PDF — Sudah Ada ✅
| Dokumen | SlipService | Controller Endpoint |
|---------|-------------|---------------------|
| CSRQ    | `CsrqSlipService` | `GET /api/csrq/{id}/slip` |
| CSRV    | `CsrvSlipService` | `GET /api/csrv/{id}/slip` |
| CSO     | `CsoSlipService`  | `GET /api/cso/{id}/slip`  |
| CSDO    | `CsdoSlipService` | `GET /api/csdo/{id}/slip` |
| CSA     | `CsaSlipService`  | `GET /api/csa/{id}/slip`  |

### PDF — Belum Ada ❌
| Dokumen | Yang Perlu Dibuat |
|---------|-------------------|
| CSRN (Return Note) | `CsrnSlipService` + endpoint di `CsrnController` |
| Customer Billing (Invoice/TN) | `CustomerBillingSlipService` + endpoint di `CustomerBillingController` |
| Supplier Billing (PO/GR) | `SupplierBillingSlipService` + endpoint di `SupplierBillingController` |

### Excel — Sudah Ada ✅ (di `ReportExcelService` + `ReportController`)
| Method | Endpoint |
|--------|----------|
| `exportTransactionReport` | `GET /api/report/{type}/export` |
| `exportSupplierBookValue` | `GET /api/report/supplier-book-value/export` |
| `exportCustomerInventory` | `GET /api/report/customer-inventory/export` |
| `exportSettlementSummary` | `GET /api/report/settlement-summary/export` |
| `exportSettlementDetail`  | `GET /api/report/settlement-detail/{id}/export` |
| `exportConsignmentSetup`  | `GET /api/report/consignment-setup/export` |
| `exportReservations`      | `GET /api/report/reservations/export` |

### Excel — Belum Ada ❌
| Report | Yang Perlu Dibuat |
|--------|-------------------|
| Supplier Billing export | Method di `ReportExcelService` + endpoint di `ReportController` |
| Customer Billing export | Method di `ReportExcelService` + endpoint di `ReportController` |

---

## Requirements Detail

### R1 — CSRN Return Note PDF
- Endpoint : `GET /api/csrn/{id}/slip`
- File     : `CsrnSlipService.java` (ikuti pola `CsaSlipService`)
- Header   : Doc No, Status, Company, Store, Supplier Code, Contract, Return Type, Reason Code, Released By/At
- Item table: No | Item Code | UOM | Qty Returned | Actual Qty Received | Condition
- Footer   : Signature box — Returned By | Accepted By
- Filename : `CSRN-{id}.pdf`

### R2 — Customer Billing Invoice/TN PDF
- Endpoint : `GET /api/customer-billing/{id}/slip`
- File     : `CustomerBillingSlipService.java`
- Header   : Doc No, Status, Company, Store, Customer Code, Period (fromDate–toDate), Released At
- Item table: No | Item Code | UOM | Sales Qty | Return Qty | Billing Qty | Unit Price | Amount
- Total section: Subtotal, Grand Total (IDR)
- Footer   : Signature box — Prepared By | Authorized By
- Filename : `CBR-{id}.pdf`

### R3 — Supplier Billing PO/GR PDF
- Endpoint : `GET /api/supplier-billing/{id}/slip`
- File     : `SupplierBillingSlipService.java`
- Header   : Doc No, Status, Company, Store, Supplier Code, Contract, Period (fromDate–toDate), Released At
- Item table: No | Item Code | UOM | Sales Qty | Return Qty | BF Qty | Billing Qty | CF Qty | Unit Cost | Amount
- Total section: Grand Total (IDR)
- Footer   : Signature box — Prepared By | Authorized By
- Filename : `SCBR-{id}.pdf`

### R4 — Supplier Billing Excel Export
- Endpoint : `GET /api/report/supplier-billing/export`
- Params   : `company`, `store`, `supplierCode`, `supplierContract`, `status`, `fromDate`, `toDate`
- Sheet    : "Supplier Billing"
- Columns  : Doc No | Supplier Code | Contract | Store | Period | From Date | To Date | Status | Item Code | UOM | Sales Qty | Return Qty | BF Qty | Billing Qty | CF Qty | Unit Cost | Amount
- Formatting: ikuti pola `exportSettlementDetail` di `ReportExcelService`
- Filename : `SupplierBilling_{fromDate}_{toDate}.xlsx`

### R5 — Customer Billing Excel Export
- Endpoint : `GET /api/report/customer-billing/export`
- Params   : `company`, `store`, `customerCode`, `status`, `fromDate`, `toDate`
- Sheet    : "Customer Billing"
- Columns  : Doc No | Customer Code | Store | Period | From Date | To Date | Status | Item Code | UOM | Sales Qty | Return Qty | Billing Qty | Unit Price | Amount
- Formatting: ikuti pola `exportSettlementDetail` di `ReportExcelService`
- Filename : `CustomerBilling_{fromDate}_{toDate}.xlsx`
