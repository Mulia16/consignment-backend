# Consignment Management System (CMS)

Sistem manajemen konsinyasi berbasis microservices dengan Spring Boot 3.3, Spring Cloud Gateway, dan PostgreSQL.

## Prasyarat

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (sudah include Docker Compose)
- Port berikut harus bebas: `5432`, `8080`, `8081`, `8082`, `8083`, `8084`, `8085`, `8761`, `27017`

## Cara Menjalankan

```bash
docker compose up --build -d
```

Tunggu sekitar 60–90 detik hingga semua service healthy:

```bash
docker compose ps
```

Semua service harus berstatus `healthy` sebelum bisa digunakan.

> Saat pertama kali jalan, Flyway otomatis menjalankan semua migration (V1–V6) termasuk seed master data untuk development/testing.

## Services

| Service             | Port  | Deskripsi                        |
|---------------------|-------|----------------------------------|
| API Gateway         | 8080  | Entry point semua request        |
| Service Registry    | 8761  | Eureka dashboard                 |
| Auth Service        | 8083  | Login, register, validasi token  |
| Consignment Service | 8082  | Core bisnis konsinyasi           |
| Inventory Service   | 8081  | Manajemen inventori              |
| Email Service       | 8084  | Pengiriman email                 |
| Batch Job Service   | 8085  | Batch processing & scheduling    |
| PostgreSQL          | 5432  | Database utama                   |
| MongoDB             | 27017 | Log & audit                      |

> Semua request ke consignment service melalui gateway: `http://localhost:8080/consignment/api/...`

## Default Credentials

| Username | Password  | Role       |
|----------|-----------|------------|
| admin    | secret123 | ROLE_ADMIN |

## Auth

### Login

```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "secret123"
}
```

Response:
```json
{
  "data": {
    "token": "eyJhbGci...",
    "token_type": "Bearer",
    "expires_in": 86400,
    "username": "admin",
    "roles": ["ROLE_ADMIN"]
  }
}
```

Gunakan token di semua request berikutnya:
```
Authorization: Bearer eyJhbGci...
```

## Modul Transaksi

| Modul  | Endpoint Base                        | Deskripsi                          | Status Flow                              |
|--------|--------------------------------------|------------------------------------|------------------------------------------|
| CSRQ   | `/consignment/api/csrq`              | Stock Request ke supplier          | HELD → RELEASED                          |
| CSRV   | `/consignment/api/csrv`              | Stock Receiving dari supplier      | HELD → RELEASED                          |
| CSO    | `/consignment/api/cso`               | Customer Sales Order               | HELD → RELEASED                          |
| CSDO   | `/consignment/api/csdo`              | Delivery Order ke customer         | HELD → RELEASED → REVERSED               |
| CSR    | `/consignment/api/csr`               | Stock Return dari customer         | HELD → RELEASED → COMPLETED              |
| CSA    | `/consignment/api/csa`               | Stock Adjustment (ADJ IN/OUT)      | HELD → RELEASED                          |
| CSRN   | `/consignment/api/csrn`              | Stock Return Note ke supplier      | HELD → UPDATED (auto-create CSRN-C)      |
| CSRN-C | `/consignment/api/csrn-c`            | Stock Return Collect (actual qty)  | HELD → UPDATED (post inventory)          |
| Settlement | `/consignment/api/settlement`    | Penagihan konsinyasi               | HELD → READY_FOR_BILLING → BILLED → SETTLED |

## Master Data (Seed)

Saat pertama kali docker dijalankan, data berikut sudah tersedia otomatis (V6 migration):

**Companies:** `COMP01`, `COMP02`

**Stores:** `STORE01`, `STORE02` (COMP01) — `STORE03` (COMP02)

**Suppliers & Contracts:**

| Supplier    | Contract           | Store  | Items                          |
|-------------|--------------------|--------|--------------------------------|
| SUPP001     | CONTRACT-2024-001  | STORE01, STORE02 | ITEM001–ITEM004    |
| SUPP002     | CONTRACT-2024-002  | STORE01, STORE03 | ITEM005–ITEM008    |
| INT-SUPP01  | *(internal)*       | STORE01 → STORE02 | ITEM001–ITEM003  |

**Items:**

| Item Code | Nama              | Harga         |
|-----------|-------------------|---------------|
| ITEM001   | Laptop Pro 15     | Rp 15.000.000 |
| ITEM002   | Laptop Air 13     | Rp 10.000.000 |
| ITEM003   | Tablet Pro 10     | Rp 7.000.000  |
| ITEM004   | Smartphone X      | Rp 12.000.000 |
| ITEM005   | Monitor 27 inch   | Rp 8.000.000  |
| ITEM006   | Wireless Keyboard | Rp 1.500.000  |
| ITEM007   | Wireless Mouse    | Rp 800.000    |
| ITEM008   | SSD 1TB           | Rp 2.500.000  |

Cek master data via API:
```bash
GET http://localhost:8080/consignment/api/master-data/companies
GET http://localhost:8080/consignment/api/master-data/stores?company=COMP01
GET http://localhost:8080/consignment/api/master-data/suppliers?company=COMP01&store=STORE01
GET http://localhost:8080/consignment/api/master-data/contracts?company=COMP01&store=STORE01&supplierCode=SUPP001
GET http://localhost:8080/consignment/api/master-data/items?company=COMP01&store=STORE01&supplierCode=SUPP001&supplierContract=CONTRACT-2024-001
```

## Postman Collection

Import file `docs/postman-collection.json` ke Postman. Collection sudah include:
- Auth (login dengan auto-save token)
- Semua modul transaksi (CSRQ, CSRV, CSO, CSDO, CSR, CSA, CSRN, CSRN-C)
- Settlement
- Master Data
- Reports

Set variable `baseUrl = http://localhost:8080` dan jalankan Login terlebih dahulu — token otomatis tersimpan ke collection variable.

## Database Migration

| Version | File                                  | Isi                                      |
|---------|---------------------------------------|------------------------------------------|
| V1      | `V1__initial_schema.sql`              | Semua tabel core (CSRQ, CSRV, CSO, dll)  |
| V2      | `V2__item_setup_add_fields.sql`       | Tambah kolom item_name, variant, dll     |
| V3      | `V3__cso_shipping_fields.sql`         | Tambah kolom shipping di CSO             |
| V4      | `V4__csr_csoDocNo_csrn_tables.sql`    | Tabel CSRN dan CSRN-C                    |
| V5      | `V5__csrn_c_actual_qty_status.sql`    | Tambah actual_qty, status di CSRN-C      |
| V6      | `V6__seed_master_data.sql`            | Seed data items, suppliers, inventory    |

## Menghentikan Services

```bash
docker compose down
```

Reset total (database dihapus, seed akan jalan ulang):

```bash
docker compose down -v
docker compose up --build -d
```

## Troubleshooting

**Service tidak healthy setelah 2 menit:**
```bash
docker compose logs <nama-service> --tail 50
```

**Cek Flyway migration berhasil:**
```bash
docker compose logs consignment-service | grep -i flyway
```

**Reset total (fresh start):**
```bash
docker compose down -v
docker compose up --build -d
```

## Dokumentasi Lanjutan

- `docs/api-flow.md` — sequence diagram lengkap semua flow bisnis
- `docs/postman-collection.json` — Postman collection siap pakai
- `docs/sprint-planning.md` — Sprint planning & roadmap pengembangan
