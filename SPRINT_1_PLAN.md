# Sprint 1 Plan (E01 + E02 + E03)

## Goal
Deliver baseline platform, master sync endpoints, and consignment item supplier setup APIs with core business-rule validation.

## In Scope
- Foundation: profiles, PostgreSQL/MyBatis baseline, error standardization, correlation-id, security scaffolding.
- Master Sync: item, supplier, contract, company, store, customer, reason sync.
- Setup Module: external/internal supplier setup APIs and rule enforcement.

## Deliverables
- `JIRA_IMPORT_SPRINT1.csv` ready for import.
- Running API endpoints for E02 and E03.
- Validations for rules T028-T031.
- Updated docs for endpoint usage.
- Operational baseline delivered for CSRQ, CSRV, and CSO.

## Endpoint Target (Sprint 1)
- `POST /api/acmm/master-sync/items`
- `POST /api/acmm/master-sync/suppliers`
- `POST /api/acmm/master-sync/contracts`
- `POST /api/acmm/master-sync/companies`
- `POST /api/acmm/master-sync/stores`
- `POST /api/acmm/master-sync/customers`
- `POST /api/acmm/master-sync/reasons`
- `GET /api/consignment-setup/items`
- `GET /api/consignment-setup/item/{itemCode}`
- `POST /api/consignment-setup/item/{itemCode}/external-supplier`
- `PUT /api/consignment-setup/item/{itemCode}/external-supplier/{id}`
- `DELETE /api/consignment-setup/item/{itemCode}/external-supplier/{id}`
- `POST /api/consignment-setup/item/{itemCode}/internal-supplier`

## Definition of Done (Sprint 1)
- Endpoints implemented and pass happy-path manual verification.
- Rule validations return deterministic 4xx responses.
- Correlation-id is propagated in request/response headers.
- Build compiles in IDE diagnostics.
- API docs updated in README or dedicated markdown.

## Risks
- Local machine currently has no Maven executable for full build verification.
- Integration to ACMM, SMTP, and SQS still scaffold-level and not yet connected to real endpoints.

## Next Sprint Preview
- Build CSDO, CSR, and CSA baseline with status transitions.
- Add SQS consumer/producer runtime integration and DLQ handling.
- Start settlement compute services and report query baseline.
