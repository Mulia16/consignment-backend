# CMS API Examples dengan Request & Response

Base URL: `http://localhost:8080` (via API Gateway)

Auth: Semua endpoint (kecuali `/auth/*`) butuh header `Authorization: Bearer <token>`

---

## 1. Auth Service

### POST /auth/login
**Request:**
```json
{
  "username": "admin",
  "password": "secret123"
}
```
**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXX0.sig"
}
```
**Response 401:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Bad credentials",
  "timestamp": "2024-01-15T08:00:00Z"
}
```

### POST /auth/register
**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "P@ssw0rd!"
}
```
**Response 200:**
```json
{
  "message": "User registered successfully"
}
```

### POST /auth/validate
**Headers:** `Authorization: Bearer <token>`
**Response 200:**
```json
{
  "valid": true,
  "username": "admin"
}
```

---

## 2. Master Data Sync

### POST /consignment/api/acmm/master-sync/items
**Request:**
```json
{
  "records": [
    {
      "code": "ITEM001",
      "attributes": {
        "hierarchy": "ELECTRONICS",
        "itemModel": "Samsung Galaxy S24",
        "syncFlag": true
      }
    }
  ]
}
```
