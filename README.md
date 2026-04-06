# Consignment Microservices

Sistem manajemen konsinyasi berbasis microservices dengan Spring Boot 3.3, Spring Cloud Gateway, dan PostgreSQL.

## Prasyarat

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (sudah include Docker Compose)
- Port berikut harus bebas: `5432`, `8080`, `8081`, `8082`, `8083`, `8084`, `8085`, `8761`, `27017`

## Cara Menjalankan

Cukup satu perintah:

```bash
docker compose up --build -d
```

Tunggu sekitar 60–90 detik hingga semua service healthy. Cek status:

```bash
docker compose ps
```

Semua service harus berstatus `healthy` sebelum bisa digunakan.

## Services

| Service            | Port  | Deskripsi                        |
|--------------------|-------|----------------------------------|
| API Gateway        | 8080  | Entry point semua request        |
| Service Registry   | 8761  | Eureka dashboard                 |
| Auth Service       | 8083  | Login, register, validasi token  |
| Consignment Service| 8082  | Core bisnis konsinyasi           |
| Inventory Service  | 8081  | Manajemen inventori              |
| Email Service      | 8084  | Pengiriman email                 |
| Batch Job Service  | 8085  | Batch processing                 |
| PostgreSQL         | 5432  | Database utama                   |
| MongoDB            | 27017 | Log & audit                      |

## Auth API

Semua request melalui API Gateway di `http://localhost:8080`.

### Login

```bash
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "secret123"
}
```

Response sukses (`200`):
```json
{
  "token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

Response gagal (`401`):
```json
{
  "error": "Invalid username or password"
}
```

### Register

```bash
POST /auth/register
Content-Type: application/json

{
  "username": "user1",
  "email": "user1@example.com",
  "password": "mypassword"
}
```

### Validasi Token

```bash
POST /auth/validate
Authorization: Bearer <token>
```

### Menggunakan Token

Sertakan header `Authorization` di setiap request ke service lain:

```bash
Authorization: Bearer eyJhbGci...
```

## Default Credentials

| Username | Password   | Role       |
|----------|------------|------------|
| admin    | secret123  | ROLE_ADMIN |

## Menghentikan Services

```bash
docker compose down
```

Untuk menghapus data (database akan reset):

```bash
docker compose down -v
```

## Troubleshooting

**Service tidak healthy setelah 2 menit:**
```bash
docker compose logs <nama-service> --tail 50
```

**Reset total (fresh start):**
```bash
docker compose down -v
docker compose up --build -d
```
