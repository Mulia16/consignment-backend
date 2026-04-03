# CMS API Flow Documentation

Platform microservices Spring Boot — semua request masuk melalui API Gateway (port 8080).

---

## Arsitektur Platform

```mermaid
graph TD
    Client([Client / Frontend])
    GW[API Gateway :8080]
    SR[Service Registry Eureka :8761]
    AUTH[Auth Service :8083]
    CS[Consignment Service :8082]
    INV[Inventory Service :8081]
    EMAIL[Email Service :8084]
    BATCH[Batch Job Service :8085]
    DB[(PostgreSQL)]
    MONGO[(MongoDB Logs)]

    Client --> GW
    GW --> AUTH
    GW --> CS
    GW --> INV
    GW --> EMAIL
    GW --> BATCH
    GW -.->|discovery| SR
    CS --> DB
    CS --> MONGO
    CS -.->|internal call| INV
    CS -.->|internal call| EMAIL
```

---

## Status Lifecycles

| Dokumen | Status Flow |
|---------|-------------|
| CSRQ | `HELD` → `RELEASED` \| `CANCELLED` |
| CSRV | `HELD` → `RELEASED` |
| CSO | `HELD` → `RELEASED` \| `ERROR` |
| CSDO | `HELD` → `RELEASED` → `REVERSED` |
| CSR | `HELD` → `RELEASED` → `COMPLETED` |
| CSA | `HELD` → `RELEASED` |
| Settlement | `HELD` → `READY_FOR_BILLING` → `BILLED` → `SETTLED` |

---

## Flow 0: Authentication

Semua request (kecuali `/auth/*`) memerlukan JWT token di header `Authorization: Bearer <token>`.

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway
    participant A as Auth Service

    C->>GW: POST /auth/login { username, password }
    GW->>A: forward request
    A-->>GW: { token: "eyJ..." }
    GW-->>C: { token: "eyJ..." }

    Note over C: Simpan token, gunakan di semua request berikutnya

    C->>GW: POST /auth/validate (Header: Authorization: Bearer <token>)
    GW->>A: forward request
    A-->>GW: { valid: true, username: "admin" }
    GW-->>C: { valid: true, username: "admin" }
```

**Endpoints:**
- `POST /auth/login` — dapat token
- `POST /auth/register` — daftarkan user baru
- `POST /auth/validate` — validasi token (digunakan internal oleh gateway)

---

## Flow 1: Setup — Master Sync + Consignment Setup

Sebelum transaksi bisa dilakukan, master data harus disync dan item-supplier mapping harus dikonfigurasi.

```mermaid
sequenceDiagram
    participant ACMM as ACMM / Admin
    participant GW as API Gateway
    participant CS as Consignment Service
    participant DB as PostgreSQL

    Note over ACMM,DB: Step 1 — Sync Master Data

    ACMM->>GW: POST /consignment/api/acmm/master-sync/items
    GW->>CS: forward
    CS->>DB: upsert consignment_item_setup
    CS-->>GW: { entity: "items", upserted: 5 }
    GW-->>ACMM: sync result

    ACMM->>GW: POST /consignment/api/acmm/master-sync/item-prices
    GW->>CS: forward
    CS->>DB: upsert consignment_item_price
    CS-->>GW: { entity: "item-prices", upserted: 5 }

    ACMM->>GW: POST /consignment/api/acmm/master-sync/suppliers
    GW->>CS: forward
    CS-->>GW: sync result

    Note over ACMM,DB: Step 2 — Consignment Setup (item-supplier mapping)

    ACMM->>GW: POST /consignment/api/consignment-setup/item/{itemCode}/external-supplier
    GW->>CS: forward { supplierCode, supplierType, contractNumber, consigneeCompany, consigneeStore }
    CS->>DB: insert consignment_external_supplier
    CS-->>GW: ExternalSupplierSetupResponse
    GW-->>ACMM: setup saved

    ACMM->>GW: GET /consignment/api/consignment-setup/items
    GW->>CS: forward
    CS->>DB: query all items with suppliers
    CS-->>GW: list of ConsignmentSetupItemResponse
    GW-->>ACMM: item-supplier mapping list
```

**Entities yang bisa disync:** `items`, `item-prices`, `suppliers`, `contracts`, `companies`, `stores`, `customers`, `reasons`

---

## Flow 2: Inbound — CSRQ → CSRV

Alur permintaan stok dari supplier dan penerimaan fisik barang.

```mermaid
sequenceDiagram
    participant U as User / Warehouse
    participant GW as API Gateway
    participant CS as Consignment Service
    participant INV as Inventory Service
    participant DB as PostgreSQL

    Note over U,DB: Step 1 — Buat Stock Request (CSRQ)

    U->>GW: POST /consignment/api/csrq
    GW->>CS: { company, store, supplierCode, supplierContract, items[] }
    CS->>DB: insert csrq_header + csrq_detail (status=HELD)
    CS-->>GW: CsrqResponse { id, docNo, status: HELD }
    GW-->>U: CSRQ created

    U->>GW: PUT /consignment/api/csrq/{id}/release
    GW->>CS: release CSRQ
    CS->>DB: update status = RELEASED, set releasedAt
    CS-->>GW: CsrqResponse { status: RELEASED }
    GW-->>U: CSRQ released

    Note over U,DB: Step 2 — Terima Stok dari Supplier (CSRV)

    U->>GW: POST /consignment/api/csrv
    GW->>CS: { company, receivingStore, supplierCode, supplierContract, supplierDoNo, items[] }
    CS->>DB: insert csrv_header + csrv_detail (status=HELD)
    CS-->>GW: CsrvResponse { id, docNo, status: HELD }
    GW-->>U: CSRV created

    U->>GW: PUT /consignment/api/csrv/{id}/release
    GW->>CS: release CSRV
    CS->>DB: update status = RELEASED
    CS->>INV: update inventory (add stock)
    CS->>DB: update supplier_book_value_inventory
    CS-->>GW: CsrvResponse { status: RELEASED }
    GW-->>U: CSRV released — stok masuk ke inventory
```

**Auto-create CSRV dari ACMM:**
```
POST /consignment/api/acmm/csrv/auto-create
```
Digunakan oleh integrasi ACMM/SQS untuk membuat CSRV secara otomatis.

---

## Flow 3: Outbound — CSO → CSDO

Alur order stok ke customer dan pengiriman barang.

```mermaid
sequenceDiagram
    participant U as User / Sales
    participant GW as API Gateway
    participant CS as Consignment Service
    participant INV as Inventory Service
    participant EMAIL as Email Service
    participant DB as PostgreSQL

    Note over U,DB: Step 1 — Buat Customer Stock Order (CSO)

    U->>GW: POST /consignment/api/cso
    GW->>CS: { company, store, customerCode, supplierCode, items[], autoGenerateCsdo }
    CS->>DB: insert cso_header + cso_detail (status=HELD)
    CS-->>GW: CsoResponse { id, docNo, status: HELD }
    GW-->>U: CSO created

    U->>GW: PUT /consignment/api/cso/{id}/release (Header: X-User: user01)
    GW->>CS: release CSO
    CS->>INV: POST reservation (ALLOCATE + FORECAST)
    INV-->>CS: reservation confirmed
    CS->>DB: update status = RELEASED, set releasedAt, releasedBy
    alt autoGenerateCsdo = true
        CS->>DB: auto-create csdo_header + csdo_detail (status=HELD)
    end
    CS-->>GW: CsoResponse { status: RELEASED }
    GW-->>U: CSO released

    Note over U,DB: Step 2 — Buat Delivery Order (CSDO)

    alt Manual CSDO creation
        U->>GW: POST /consignment/api/csdo/transfer/{csoId}
        GW->>CS: { requireGenerateCdo, shippingMode, transporter, createdBy }
        CS->>DB: insert csdo_header + csdo_detail (status=HELD)
        CS-->>GW: CsdoResponse { id, docNo, status: HELD }
        GW-->>U: CSDO created
    end

    U->>GW: PUT /consignment/api/csdo/{id}/release
    GW->>CS: release CSDO
    CS->>DB: update status = RELEASED
    CS->>DB: update customer_consignment_inventory (add qty)
    CS->>EMAIL: send delivery notification
    CS-->>GW: CsdoResponse { status: RELEASED }
    GW-->>U: CSDO released — barang terkirim ke customer

    Note over U,DB: Optional — Reverse CSDO

    U->>GW: PUT /consignment/api/csdo/{id}/reverse
    GW->>CS: reverse CSDO
    CS->>DB: update status = REVERSED
    CS->>DB: reverse customer_consignment_inventory
    CS-->>GW: CsdoResponse { status: REVERSED }
    GW-->>U: CSDO reversed
```

**Auto-create CSO dari ACMM/POS:**
```
POST /consignment/api/acmm/cso/auto-create
```

---

## Flow 4: Return — CSR

Alur pengembalian stok dari customer ke supplier.

```mermaid
sequenceDiagram
    participant U as User / Warehouse
    participant GW as API Gateway
    participant CS as Consignment Service
    participant DB as PostgreSQL

    U->>GW: POST /consignment/api/csr
    GW->>CS: { company, store, supplierCode, supplierContract, reasonCode, items[] }
    CS->>DB: insert csr_header + csr_detail (status=HELD)
    CS-->>GW: CsrResponse { id, docNo, status: HELD }
    GW-->>U: CSR created

    U->>GW: PUT /consignment/api/csr/{id}/release
    GW->>CS: release CSR
    CS->>DB: update status = RELEASED
    CS-->>GW: CsrResponse { status: RELEASED }
    GW-->>U: CSR released

    Note over U,DB: Optional — Update actual qty per detail line

    U->>GW: PUT /consignment/api/csr/{id}/detail/{detailId}/actual-qty
    GW->>CS: { actualQty: 9 }
    CS->>DB: update csr_detail.actual_qty
    CS-->>GW: CsrResponse updated
    GW-->>U: actual qty updated

    U->>GW: PUT /consignment/api/csr/{id}/complete
    GW->>CS: complete CSR
    CS->>DB: update status = COMPLETED, set completedAt
    CS->>DB: deduct customer_consignment_inventory (using actualQty if set, else qty)
    CS->>DB: update supplier_book_value_inventory
    CS-->>GW: CsrResponse { status: COMPLETED }
    GW-->>U: CSR completed — inventory deducted
```

---

## Flow 5: Adjustment — CSA

Alur penyesuaian stok (ADJ IN / ADJ OUT) dengan keputusan settlement.

```mermaid
sequenceDiagram
    participant U as User / Supervisor
    participant GW as API Gateway
    participant CS as Consignment Service
    participant DB as PostgreSQL

    U->>GW: POST /consignment/api/csa
    GW->>CS: { company, store, transactionType: ADJ_IN|ADJ_OUT, items[{settlementDecision}] }
    CS->>DB: insert csa_header + csa_detail (status=HELD)
    CS-->>GW: CsaResponse { id, docNo, status: HELD }
    GW-->>U: CSA created

    U->>GW: PUT /consignment/api/csa/{id}/release (Header: X-User: user01)
    GW->>CS: release CSA
    CS->>DB: update status = RELEASED, set releasedAt, releasedBy
    alt transactionType = ADJ_IN
        CS->>DB: add to supplier_book_value_inventory
    else transactionType = ADJ_OUT
        CS->>DB: deduct from supplier_book_value_inventory
    end
    alt settlementDecision = INCLUDE_IN_SETTLEMENT
        Note over CS: akan dimasukkan ke settlement batch berikutnya
    end
    CS-->>GW: CsaResponse { status: RELEASED }
    GW-->>U: CSA released
```

**`settlementDecision` values:**
- `INCLUDE_IN_SETTLEMENT` — masuk ke perhitungan settlement
- `EXCLUDE_FROM_SETTLEMENT` — tidak masuk settlement

---

## Flow 6: Settlement

Alur penagihan dan penyelesaian pembayaran consignment.

```mermaid
sequenceDiagram
    participant BATCH as Batch Job / Admin
    participant GW as API Gateway
    participant CS as Consignment Service
    participant DB as PostgreSQL

    Note over BATCH,DB: Step 1 — Generate Settlement (batch atau manual)

    BATCH->>GW: POST /consignment/api/settlement/generate
    GW->>CS: { company, store, settlementType, customerCode/supplierCode, fromDate, toDate }
    CS->>DB: query released documents (CSO/CSRV/CSR/CSDO) dalam periode
    CS->>DB: insert settlement_request_header (status=HELD)
    CS->>DB: insert settlement_request_detail per line item
    CS-->>GW: SettlementResponse { id, docNo, status: HELD, totalAmount }
    GW-->>BATCH: settlement generated

    Note over BATCH,DB: Optional — Tambah detail dari dokumen spesifik

    BATCH->>GW: POST /consignment/api/settlement/{id}/details/from-documents
    GW->>CS: { documents: [{ documentType, documentId, unitPrice }] }
    CS->>DB: insert additional settlement_request_detail
    CS-->>GW: SettlementResponse updated

    Note over BATCH,DB: Step 2 — Lifecycle: HELD → READY_FOR_BILLING → BILLED → SETTLED

    BATCH->>GW: PUT /consignment/api/settlement/{id}/prepare-for-billing
    GW->>CS: update status
    CS->>DB: status = READY_FOR_BILLING, set readyForBillingAt
    CS-->>GW: SettlementResponse { status: READY_FOR_BILLING }

    BATCH->>GW: PUT /consignment/api/settlement/{id}/mark-as-billed
    GW->>CS: update status
    CS->>DB: status = BILLED, set billedAt
    CS-->>GW: SettlementResponse { status: BILLED }

    BATCH->>GW: PUT /consignment/api/settlement/{id}/mark-as-settled
    GW->>CS: update status
    CS->>DB: status = SETTLED, set settledAt
    CS-->>GW: SettlementResponse { status: SETTLED }
    GW-->>BATCH: settlement complete
```

**Trigger batch settlement via Batch Job Service:**
```
POST /batch/settlement/trigger?businessDate=2024-01-31
```

---

## Flow 7: Reports (R01–R12)

Semua report endpoint menggunakan `GET` dengan query params filter.

```mermaid
sequenceDiagram
    participant U as User / Analyst
    participant GW as API Gateway
    participant CS as Consignment Service
    participant DB as PostgreSQL

    U->>GW: GET /consignment/api/reports/{reportType}?company=&store=&fromDate=&toDate=
    GW->>CS: forward with query params
    CS->>DB: execute report query
    CS-->>GW: array of report rows
    GW-->>U: report data
```

| Report | Endpoint | Deskripsi |
|--------|----------|-----------|
| R01 | `GET /consignment/api/reports/csrq` | CSRQ transactions by period |
| R02 | `GET /consignment/api/reports/csrv` | CSRV transactions by period |
| R03 | `GET /consignment/api/reports/cso` | CSO transactions by period |
| R04 | `GET /consignment/api/reports/csdo` | CSDO transactions by period |
| R05 | `GET /consignment/api/reports/csr` | CSR transactions by period |
| R06 | `GET /consignment/api/reports/csa` | CSA transactions by period |
| R07 | `GET /consignment/api/reports/settlement-summary` | Settlement summary |
| R08 | `GET /consignment/api/reports/settlement-detail/{id}` | Settlement detail lines |
| R09 | `GET /consignment/api/reports/supplier-book-value` | Supplier BV inventory |
| R10 | `GET /consignment/api/reports/customer-inventory` | Customer consignment on-hand |
| R11 | `GET /consignment/api/reports/reservations` | Open reservations |
| R12 | `GET /consignment/api/reports/consignment-setup` | Item-supplier mapping |

**Trigger report pre-computation:**
```
POST /batch/report/trigger?reportDate=2024-01-30
```

---

## Flow 8: End-to-End Business Flow

Gambaran lengkap alur bisnis dari setup hingga settlement.

```mermaid
flowchart TD
    A[Master Data Sync] --> B[Consignment Setup\nitem-supplier mapping]
    B --> C

    subgraph INBOUND [Inbound Flow]
        C[CSRQ\nStock Request\nHELD → RELEASED] --> D[CSRV\nStock Receive\nHELD → RELEASED]
    end

    subgraph OUTBOUND [Outbound Flow]
        E[CSO\nCustomer Order\nHELD → RELEASED] --> F[CSDO\nDelivery Order\nHELD → RELEASED]
        F --> G{Reversal?}
        G -->|Yes| H[CSDO REVERSED]
        G -->|No| I[Delivered]
    end

    subgraph RETURN [Return Flow]
        J[CSR\nStock Return\nHELD → RELEASED → COMPLETED]
    end

    subgraph ADJUSTMENT [Adjustment Flow]
        K[CSA\nStock Adjustment\nHELD → RELEASED\nADJ_IN / ADJ_OUT]
    end

    subgraph SETTLEMENT [Settlement Flow]
        L[Generate Settlement\nHELD] --> M[Prepare for Billing\nREADY_FOR_BILLING]
        M --> N[Mark as Billed\nBILLED]
        N --> O[Mark as Settled\nSETTLED]
    end

    D --> E
    I --> J
    I --> K
    J --> L
    K --> L
    D --> L
```

---

## Headers Penting

| Header | Endpoint | Keterangan |
|--------|----------|------------|
| `Authorization: Bearer <token>` | Semua endpoint (kecuali `/auth/*`) | JWT token dari login |
| `X-User: <username>` | `PUT /cso/{id}/release`, `PUT /csa/{id}/release` | Username yang melakukan release |
| `Content-Type: application/json` | Semua POST/PUT | Wajib untuk request body |

---

## Error Responses

Semua error mengikuti format:

```json
{
  "error": "string",
  "message": "string",
  "timestamp": "2024-01-15T08:00:00Z"
}
```

| HTTP Status | Kondisi |
|-------------|---------|
| 400 | Validation error, status transition tidak valid |
| 401 | Token tidak ada atau expired |
| 404 | Resource tidak ditemukan |
| 500 | Internal server error |
