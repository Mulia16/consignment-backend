# Consignment Module Task Backlog

## 1) Objective
Build a production-ready Consignment Module integrated with ACMM, covering setup, transactions, inventory, settlement, reporting, batch jobs, security, and embedded portal support.

## 2) Epic Map
- E01 Platform Foundation
- E02 Master Data Sync (ACMM -> Consignment)
- E03 Consignment Item Supplier Setup
- E04 CSRQ (Consignment Stock Request)
- E05 CSRV (Consignment Stock Receiving)
- E06 CSO (Consignment Stock Out)
- E07 CSDO (Consignment Delivery Order)
- E08 CSR (Consignment Stock Return)
- E09 CSA (Consignment Stock Adjustment)
- E10 Inventory Engine
- E11 Customer Settlement
- E12 Supplier Settlement
- E13 Integration & SQS
- E14 API Logging (MongoDB)
- E15 Reporting
- E16 Batch Jobs
- E17 Email Service
- E18 Embedded Portal (JSP + Bootstrap + jQuery)
- E19 Security & Access Control
- E20 QA, UAT, Go-Live

## 3) Detailed Tasks

### E01 Platform Foundation
- T001 Create service boundaries and package conventions.
- T002 Configure profiles: dev, sit, uat, prod.
- T003 Setup PostgreSQL datasource and migration baseline.
- T004 Setup MongoDB connection for API logs.
- T005 Setup MyBatis base config and mapper scanning.
- T006 Setup global API response format and exception handler.
- T007 Setup JWT validation and role scaffolding.
- T008 Integrate secret retrieval via AWS Secret Manager or Vault.
- T009 Add correlation-id interceptor/filter.
- T010 Setup CI build and test pipeline.

### E02 Master Data Sync
- T011 Implement Item Master sync with sync_flag filter.
- T012 Implement Supplier Master sync.
- T013 Implement Supplier Contract sync (purchase method consignment).
- T014 Implement Company Master sync.
- T015 Implement Store Master sync.
- T016 Implement Customer Master sync.
- T017 Implement Reason List sync.
- T018 Implement hierarchy validation (Consignment vs Outright).
- T019 Implement upsert logic and soft delete handling.
- T020 Implement retry and DLQ for sync failures.

### E03 Consignment Item Supplier Setup
- T021 Create setup schema tables and mapper.
- T022 Build API GET /api/consignment-setup/items.
- T023 Build API GET /api/consignment-setup/item/{itemCode}.
- T024 Build API POST external supplier setup.
- T025 Build API PUT external supplier setup with inventory-zero validation.
- T026 Build API DELETE external supplier setup.
- T027 Build API POST internal supplier setup.
- T028 Enforce one-store-one-supplier-per-item rule.
- T029 Enforce external/internal mutual exclusion per item-store.
- T030 Enforce external supplier type = External.
- T031 Enforce internal supplier hierarchy relation rule.
- T032 Add import/export feature for setup.

### E04 CSRQ
- T033 Create CSRQ header/detail schema and mapper.
- T034 Build API GET/POST /api/csrq.
- T035 Build API PUT /api/csrq/{id}/release.
- T036 Build API DELETE /api/csrq/{id}.
- T037 Implement CSRQ filter fields from requirements.
- T038 Validate supplier+contract+store linkage on item selection.
- T039 Implement print slip mapping to Purchase Order Slip style.
- T040 Implement auto email on release.
- T041 Add outstanding unreceived settlement support.

### E05 CSRV
- T042 Create CSRV header/detail/batch schema and mapper.
- T043 Build API GET/POST /api/csrv.
- T044 Build API PUT /api/csrv/{id}/release.
- T045 Build API POST /api/acmm/csrv/auto-create.
- T046 On release, post qty to Supplier Book Value Inventory.
- T047 Implement batch no, expiry, qty handling.
- T048 Implement print slip mapping to Receiving Slip style.

### E06 CSO
- T049 Create CSO header/detail schema and mapper.
- T050 Build API GET/POST /api/cso.
- T051 Build API PUT /api/cso/{id}/release.
- T052 Build API DELETE /api/cso/{id} for Held/Error only.
- T053 Build API POST /api/acmm/cso/auto-create.
- T054 On release, write reservation Allocate+Forecast rows.
- T055 Support Generate CSDO = Yes auto create flow.
- T056 Implement Error status behavior while keeping reservation post.
- T057 Implement reservation cleanup on delete Error by admin.
- T058 Implement print slip mapping to Sales Order Slip style.

### E07 CSDO
- T059 Create CSDO header/detail schema and mapper.
- T060 Build API GET /api/csdo.
- T061 Build API PUT /api/csdo/{id}/release.
- T062 Restrict creation source to CSO transfer only.
- T063 If require_generate_cdo=yes, move CSO reservation to CSDO reservation.
- T064 If require_generate_cdo=no, move CSO reservation to customer consignment inventory.
- T065 Implement print slip mapping to Delivery Order Slip style.

### E08 CSR (Stock Return)
- T066 Create CSR header/detail schema and mapper.
- T067 Build API GET/POST /api/csr.
- T068 Build API PUT /api/csr/{id}/release.
- T069 Build API PUT /api/csr/{id}/complete.
- T070 Restrict edit during Released to actual_qty only.
- T071 On release, send slip email to supplier.
- T072 On complete, deduct Supplier BV and Customer Consignment inventory.
- T073 Implement print slip mapping to Goods Return Slip style.

### E09 CSA (Stock Adjustment)
- T074 Create CSA header/detail schema and mapper.
- T075 Build API GET/POST /api/csa.
- T076 Build API PUT /api/csa/{id}/release.
- T077 Implement ADJ_IN default settlement to unpost sales return.
- T078 Implement ADJ_OUT default settlement to unpost sales.
- T079 Implement direct supplier BV inventory adjust options.
- T080 Implement ACMM source mapping (stock take, bin adj, etc.).
- T081 Implement print slip mapping to Inventory Adjustment Slip style.

### E10 Inventory Engine
- T082 Implement Supplier Book Value Inventory formulas.
- T083 Implement Customer Consignment Inventory updates.
- T084 Implement Consignment Reservation table updates.
- T085 Implement Unpost staging updates.
- T086 Implement available stock formula service.
- T087 Add movement journal/audit table.
- T088 Add transaction/locking strategy for consistency.

### E11 Customer Settlement
- T089 Compute billing request from customer inventory sales-return.
- T090 Create customer billing request in Held status.
- T091 Release flow for positive billing qty.
- T092 Release flow for negative billing qty.
- T093 Handle same-company outlet scenario.
- T094 Handle different-company outlet scenario.
- T095 Handle external-customer scenario.
- T096 Remove unpost and deduct inventory after successful release.

### E12 Supplier Settlement
- T097 Compute supplier billing from unpost sales-return.
- T098 Create supplier billing request in Held status.
- T099 Release positive billing qty -> PO + receiving.
- T100 Release negative billing qty -> goods return.
- T101 Implement decimal carry forward flag.
- T102 Implement costing methods: fixed, fixed %, cumulative, non-cumulative.

### E13 Integration & SQS
- T103 Provision SQS queues and DLQ pairs.
- T104 Implement consignment-master-sync consumer.
- T105 Implement consignment-pos-sales consumer.
- T106 Implement consignment-csrv-auto consumer.
- T107 Implement consignment-cso-auto consumer.
- T108 Implement consignment-adj-auto consumer.
- T109 Implement consignment-available-stock producer.
- T110 Implement consignment-settlement-docs producer.
- T111 Add idempotency keys and retry policy.

### E14 API Logging
- T112 Implement inbound/outbound API logging to MongoDB.
- T113 Log endpoint, payloads, status, processing time.
- T114 Include correlation id in all log entries.
- T115 Add masking for sensitive fields.
- T116 Add retention/index strategy.

### E15 Reporting
- T117 Implement Report 1 Stock Movement.
- T118 Implement Report 2 Available Stock.
- T119 Implement Report 3 Supplier Settlement.
- T120 Implement Report 4 Customer Settlement.
- T121 Implement Report 5 Item Store Supplier.
- T122 Implement Report 6 Supplier BV Inventory.
- T123 Implement Report 7 Customer Consignment Inventory.
- T124 Implement Report 8 Supplier Liability.
- T125 Implement Report 9 Consignment Stock Available.
- T126 Implement Report 10 Own Liability.
- T127 Implement Report 11/12 full variants.
- T128 Implement PDF/Excel export.

### E16 Batch Jobs
- T129 Master data sync schedule/event orchestration.
- T130 POS sales real-time processing.
- T131 Available stock push every 5 minutes.
- T132 Settlement auto-compute monthly/weekly.
- T133 Nightly report pre-computation.

### E17 Email Service
- T134 Setup SMTP sender and templates.
- T135 CSRQ release email with PDF attachment.
- T136 CSR release email with PDF attachment.
- T137 Settlement release summary email.
- T138 Email delivery retry and logging.

### E18 Embedded Portal
- T139 Setup JSP pages with Bootstrap 4 and jQuery.
- T140 Integrate SSO/session pass-through from client portal.
- T141 Build setup pages.
- T142 Build transaction pages.
- T143 Build settlement pages.
- T144 Build reports pages.
- T145 Integrate navigation into client portal shell.

### E19 Security
- T146 Implement RBAC: Admin, Store User, Supplier User.
- T147 Define endpoint authorization matrix.
- T148 Implement CORS policy for embedded usage.
- T149 Implement token auto-refresh for background jobs.
- T150 Implement access audit trail.

### E20 QA and Go-Live
- T151 Unit tests for service/business rules.
- T152 Integration tests (API + DB + queue).
- T153 Contract tests with ACMM interfaces.
- T154 Performance tests for compute/report heavy flows.
- T155 UAT checklist per module.
- T156 Cutover and data migration checklist.
- T157 Go-live runbook and rollback plan.

## 4) Priority Buckets

### P0 (must-have first)
- E01, E02, E03, E04, E05, E06, E10, E13 (minimum queues), E14 basic logging, E19 basic JWT/RBAC.

### P1 (next)
- E07, E08, E09, E11, E12, E16, E17.

### P2 (stabilization)
- E15 full report set, E18 portal hardening, E20 full non-functional readiness.

## 5) Sprint Suggestion
- Sprint 1: E01 + E02 + E03
- Sprint 2: E04 + E05
- Sprint 3: E06 + E07 + E10
- Sprint 4: E08 + E09 + E13 + E14
- Sprint 5: E11 + E12 + E17 + E16
- Sprint 6: E15 + E18 + E19 + E20

## 6) Definition of Done
- Business rule validated by unit/integration tests.
- API contract documented and versioned.
- Error handling and logging present with correlation id.
- Security checks enforced by role.
- Queue retry/DLQ behavior tested.
- UAT checklist item passed.
