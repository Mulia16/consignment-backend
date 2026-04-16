# Tasks: PDF & Excel Download Feature

## PDF — Missing Slip Services

- [x] 1. CSRN Return Note PDF
  - [x] 1.1 Buat `CsrnSlipService.java` di package `pdf/`
  - [x] 1.2 Tambah endpoint di `CsrnController.java` → `GET /api/csrn/{id}/slip`

- [x] 2. Customer Billing Invoice PDF
  - [x] 2.1 Buat `CustomerBillingSlipService.java` di package `pdf/`
  - [x] 2.2 Tambah endpoint di `CustomerBillingController.java` → `GET /api/customer-billing/{id}/slip`

- [x] 3. Supplier Billing PO/GR PDF
  - [x] 3.1 Buat `SupplierBillingSlipService.java` di package `pdf/`
  - [x] 3.2 Tambah endpoint di `SupplierBillingController.java` → `GET /api/supplier-billing/{id}/slip`

## Excel — Missing Report Exports

- [x] 4. Supplier Billing Excel Export
  - [x] 4.1 Tambah method `exportSupplierBilling(...)` di `ReportExcelService.java`
  - [x] 4.2 Tambah endpoint `GET /api/report/supplier-billing/export` di `ReportController.java`

- [x] 5. Customer Billing Excel Export
  - [x] 5.1 Tambah method `exportCustomerBilling(...)` di `ReportExcelService.java`
  - [x] 5.2 Tambah endpoint `GET /api/report/customer-billing/export` di `ReportController.java`
