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
- Done: foundation, setup persistence, master sync baseline, CSRQ baseline, CSRV baseline.
- Ongoing: CSO baseline.

### Sprint 2 - CSO and CSDO Core
Status: In Progress

Scope:
- CSO lifecycle Held -> Released | Error.
- Reservation posting (Allocate + Forecast).
- Auto-create CSO from ACMM.
- CSDO lifecycle from CSO transfer only.

Current implementation status:
- Done: CSO baseline and CSDO baseline.
- Ongoing: hardening CSO/CSDO integration with downstream settlement docs.

### Sprint 3 - CSR and CSA Core
Status: In Progress

Scope:
- CSR lifecycle Held -> Released -> Completed.
- CSA lifecycle Held -> Released with ADJ IN/OUT settlement options.
- Inventory side effects to Supplier BV and Customer Consignment inventory.

Current implementation status:
- Done: CSR and CSA baseline with status transitions and posting rules.
- Ongoing: additional validations, batch sourcing, and reconciliation edge cases.

### Sprint 4 - Settlement Engine
Status: Planned

Scope:
- Customer consignment settlement compute + billing request.
- Supplier consignment settlement compute + carry forward handling.
- Auto document actions for release scenarios.

### Sprint 5 - Integration and Messaging
Status: Planned

Scope:
- SQS consumer/producer implementation (master sync, POS, CSRV auto, CSO auto, stock push, settlement docs).
- DLQ handling and retry policy.
- Outbound API integrations to ACMM.

### Sprint 6 - Reporting and Batch
Status: Planned

Scope:
- 12 reports baseline query + export.
- Batch jobs scheduling (nightly/monthly/realtime triggers).
- Report pre-computation for heavy reports.

### Sprint 7 - Embedded Portal UX
Status: Planned

Scope:
- JSP/Spring MVC pages with Bootstrap 4 and jQuery.
- SSO/token pass-through support.
- Operational screens for setup and documents.

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
