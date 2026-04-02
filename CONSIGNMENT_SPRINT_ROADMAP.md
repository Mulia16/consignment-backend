# Consignment Module Sprint Roadmap

## Total Sprint
Total rencana: 8 sprint (2 minggu per sprint).

## Sprint Breakdown

### Sprint 1 - Foundation and Setup
Status: In Progress

Scope:
- Platform foundation (security scaffold, correlation-id, config profile).
- Master data sync baseline (ACMM inbound API).
- Consignment item supplier setup (external/internal + business rules).
- CSRQ and CSRV baseline APIs.

Current implementation status:
- Done: foundation, setup persistence, master sync baseline, CSRQ baseline, CSRV baseline, CSO baseline.

### Sprint 2 - CSO and CSDO Core
Status: Done

Scope:
- CSO lifecycle Held -> Released | Error.
- Reservation posting (Allocate + Forecast).
- Auto-create CSO from ACMM.
- CSDO lifecycle from CSO transfer only.

Current implementation status:
- Done: CSO baseline and CSDO baseline, CSO/CSDO integration with reservation posting, auto-generate CSDO on release, reversal support.

### Sprint 3 - CSR and CSA Core
Status: Done

Scope:
- CSR lifecycle Held -> Released -> Completed.
- CSA lifecycle Held -> Released with ADJ IN/OUT settlement options.
- Inventory side effects to Supplier BV and Customer Consignment inventory.

Current implementation status:
- Done: CSR and CSA baseline with status transitions and posting rules, edge case validations (settlement decision guards, actualQty fallback, detail ownership checks, idempotency on CSDO transfer).

### Sprint 4 - Settlement Engine
Status: Done

Scope:
- Customer consignment settlement compute + billing request.
- Supplier consignment settlement compute + carry forward handling.
- Auto document actions for release scenarios.

Current implementation status:
- Done: settlement compute engine (CUSTOMER + SUPPLIER), billing lifecycle (HELD → READY_FOR_BILLING → BILLED → SETTLED), batch generation with period filter and idempotency, weekly/monthly scheduler, 12 report queries (R01-R12).

### Sprint 5 - Integration and Messaging
Status: In Progress

Scope:
- SQS consumer/producer implementation (master sync, POS, CSRV auto, CSO auto, stock push, settlement docs).
- DLQ handling and retry policy.
- Outbound API integrations to ACMM.

Current implementation status:
- Done: SqsConsumer (CSO auto-create, CSRV auto-create), SqsProducer (settlement docs, stock update, master sync result), ConditionalOnProperty guard for local dev without AWS.
- Ongoing: DLQ configuration, POS sales consumer, ACMM outbound API integration.

### Sprint 6 - Reporting and Batch
Status: Done

Scope:
- 12 reports baseline query + export.
- Batch jobs scheduling (nightly/monthly/realtime triggers).
- Report pre-computation for heavy reports.

Current implementation status:
- Done: 12 report endpoints (R01-R12) via ReportController + ReportService + ReportMapper, batch-job-service with Spring Batch (NightlySettlementJob, ReportPreComputeJob), manual trigger endpoints.

### Sprint 7 - Embedded Portal UX
Status: Deferred to Frontend Team

Scope:
- Frontend UI removed from consignment-service (Thymeleaf, static assets).
- API contracts ready for frontend integration.
- All endpoints accessible via API Gateway.

### Sprint 8 - Hardening and UAT
Status: Planned

Scope:
- Performance and resilience tuning.
- Security hardening (JWT role enforcement, secret manager integration).
- UAT defect fixes and release checklist.

## Definition of Done per Sprint
- API contract documented.
- Main business rules covered by service tests.
- Persistence and status transitions validated.
- Operational documentation updated.

## Risk Notes
- Local environment belum memiliki Maven executable, sehingga verifikasi saat ini mengandalkan IDE diagnostics.
- Integrasi eksternal ACMM/SQS/email masih bertahap memakai scaffold sebelum koneksi nyata.
