# Sprint Planning — Consignment Management System

## Status Saat Ini (Per 13 April 2026)

### Sudah Selesai
- Auth Service (login, logout, register, JWT)
- API Gateway (routing, correlation ID)
- CSRQ — Stock Request (CRUD, release)
- CSRV — Stock Receiving (CRUD, release)
- CSO — Sales Order (CRUD, release, delete)
- CSDO — Delivery Order (transfer from CSO, release, reverse)
- CSR — Stock Return (CRUD, release, actual qty, complete)
- CSA — Stock Adjustment (CRUD, release)
- CSRN — Stock Return Note (CRUD, update → auto create CSRN-C)
- CSRN-C — Stock Return Collect (search, get, update actual qty, complete → post inventory)
- Settlement (create, search, batch generate, post details, billing flow)
- Reports (12 endpoint)
- Master Data (companies, stores, suppliers, contracts, items)
- Consignment Setup (item setup, external/internal supplier)
- DB Migration V1–V5

---

## Sprint 1 — 13 April s/d 17 April

**Fokus: Stabilisasi & Bug Fix + Print Slip CSRN-C**

| # | Task | Priority |
|---|------|----------|
| 1 | Testing end-to-end flow CSRN → CSRN-C (create, update, actual qty, complete) | High |
| 2 | Implementasi print slip PDF untuk CSRN-C (`GET /api/csrn-c/:id/slip`) | High |
| 3 | Implementasi `NotificationService` — kirim email saat CSRN status UPDATED (return slip ke supplier) | High |
| 4 | Validasi inventory saat CSRN create — cek consignment reservation inventory | Medium |
| 5 | Fix filter `status` di CSRN-C search (sudah ada di criteria, pastikan query benar) | Medium |
| 6 | Update postman collection — tambah endpoint slip CSRN-C | Low |

---

## Sprint 2 — 20 April s/d 24 April

**Fokus: Batch Job + Settlement Enhancement**

| # | Task | Priority |
|---|------|----------|
| 1 | Implementasi `BatchJobConfig.settlementStep()` — logic komputasi settlement otomatis | High |
| 2 | Implementasi `BatchJobConfig.reportStep()` — pre-compute report data | High |
| 3 | Implementasi `SettlementBatchScheduler` — jadwal otomatis settlement harian/mingguan | High |
| 4 | Settlement: tambah filter `fromDate` / `toDate` di search | Medium |
| 5 | Settlement: validasi dokumen tidak bisa di-post dua kali ke settlement yang sama | Medium |
| 6 | CSR: validasi `actualQty` tidak boleh melebihi `qty` saat update | Medium |
| 7 | CSRN-C: validasi `actualQty` tidak boleh melebihi `qty` saat update | Medium |

---

## Sprint 3 — 27 April s/d 1 Mei

**Fokus: Audit Trail + Document Lifecycle**

| # | Task | Priority |
|---|------|----------|
| 1 | Buat tabel `audit_log` dan implementasi audit trail untuk semua perubahan status dokumen | High |
| 2 | Endpoint `GET /api/:module/:id/history` — riwayat perubahan status dokumen | High |
| 3 | Implementasi delete/cancel untuk CSRQ (status HELD saja) | Medium |
| 4 | Implementasi delete/cancel untuk CSRV (status HELD saja) | Medium |
| 5 | Implementasi delete/cancel untuk CSRN (status HELD saja) | Medium |
| 6 | Tambah endpoint `GET /api/csrn/:id/print-listing` — print listing CSRN | Medium |
| 7 | Tambah endpoint `GET /api/csrn-c/:id/print-listing` — print listing CSRN-C | Medium |
| 8 | DB Migration V6 — tabel audit_log | High |

---

## Sprint 4 — 4 Mei s/d 8 Mei

**Fokus: Reports Enhancement + Integration Testing**

| # | Task | Priority |
|---|------|----------|
| 1 | Report CSRN — listing dengan filter lengkap + export PDF | High |
| 2 | Report CSRN-C — listing dengan filter lengkap + export PDF | High |
| 3 | Report Supplier Book Value — pastikan data CSRN-C ter-include setelah complete | High |
| 4 | Report Customer Inventory — pastikan data ter-update setelah CSRN-C complete (internal supplier) | High |
| 5 | Integration test end-to-end: CSRN → CSRN-C → inventory mutation → settlement | High |
| 6 | Performance test — query search dengan data besar (index review) | Medium |
| 7 | API documentation update (Postman collection final) | Medium |
| 8 | Docker compose production-ready config (env variables, secrets) | Low |

---

## Catatan

- **Email notifikasi** (Sprint 1) adalah blocker untuk flow CSRN karena requirement menyebut auto email return slip ke supplier saat status UPDATED
- **Batch job** (Sprint 2) penting untuk settlement otomatis — saat ini masih stub
- **Audit trail** (Sprint 3) diperlukan untuk compliance dan traceability dokumen
- **Report CSRN/CSRN-C** (Sprint 4) belum ada di report service, perlu ditambah
