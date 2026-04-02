# Consignment Management System (CMS) - Microservices Platform

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat&logo=spring)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-6DB33F?style=flat&logo=spring)
![MySQL](https://img.shields.io/badge/PostgreSQL-14+-336791?style=flat&logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green?style=flat)

Enterprise-grade Java microservices platform for **Consignment Management System (CMS)** built on Spring Boot 3, Spring Cloud, and PostgreSQL. Provides comprehensive APIs for consignment request, stock management, customer/supplier order lifecycle, and settlement processing.

---

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [System Architecture](#system-architecture)
- [Services Overview](#services-overview)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Services](#running-the-services)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Development Guide](#development-guide)
- [Troubleshooting](#troubleshooting)
- [Sprint Roadmap](#sprint-roadmap)

---

## 🚀 Quick Start

### Fastest way to get up and running:

```bash
# 1. Clone and navigate
cd consignment-module

# 2. Build all services
mvn clean package

# 3. Start service registry (Eureka)
cd service-registry
mvn spring-boot:run

# 4. In another terminal: Start inventory service
cd inventory-service
mvn spring-boot:run

# 5. In another terminal: Start consignment service
cd consignment-service
mvn spring-boot:run

# 6. In another terminal: Start API gateway
cd api-gateway
mvn spring-boot:run

# 7. Test through gateway
curl -X GET http://localhost:8080/inventory/api/v1/inventory/SKU01/availability
```

### Quick Test API calls through gateway:

**Check Inventory Availability:**
```bash
GET http://localhost:8080/inventory/api/v1/inventory/SKU01/availability
```

**Create Consignment Stock Request (CSRQ):**
```bash
POST http://localhost:8080/consignment/api/csrq
Content-Type: application/json

{
  "sku": "SKU01",
  "quantity": 10,
  "requestStore": "0001",
  "supplier": "SUP-A",
  "contract": "CONTRACT01",
  "company": "COMP01"
}
```

---

## 🐳 Docker Deployment

### Prerequisites for Docker
- Docker Engine 20.10+
- Docker Compose 2.0+

### Quick Start with Docker

```bash
# 1. Clone and navigate
cd consignment-module

# 2. Build and start all services with Docker Compose
docker-compose up --build

# Services will be available at:
# - API Gateway: http://localhost:8080
# - Service Registry (Eureka): http://localhost:8761
# - Inventory Service: http://localhost:8081
# - Consignment Service: http://localhost:8082
# - PostgreSQL: localhost:5432
# - MongoDB: localhost:27017
```

### Docker Commands

```bash
# Start services in background
docker-compose up -d

# View logs
docker-compose logs -f [service-name]

# Stop services
docker-compose down

# Rebuild specific service
docker-compose up --build [service-name]

# Clean up volumes (WARNING: deletes data)
docker-compose down -v
```

### Individual Service Builds

```bash
# Build specific service
cd [service-name]
docker build -t consignment/[service-name] .

# Run specific service
docker run -p [port]:[port] consignment/[service-name]
```

---

## 🏗️ System Architecture

### Microservices Topology

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT APPLICATIONS                           │
└──────────────────────────────────┬──────────────────────────────┘
                                   │
                                   ▼
        ┌──────────────────────────────────────────────┐
        │      API Gateway (Spring Cloud Gateway)      │
        │           Port: 8080                         │
        │   - Route requests to backend services       │
        │   - Load balancing                           │
        │   - Rate limiting & security                 │
        └──────┬──────────────────┬─────────────┬──────┘
               │                  │             │
      ┌────────▼────────┐  ┌──────▼──────┐ ┌───▼──────────┐
      │ Service Registry│  │    Inventory│ │ Consignment  │
      │  (Eureka)       │  │    Service  │ │   Service    │
      │  Port: 8761     │  │  Port: 8081 │ │  Port: 8082  │
      └─────────────────┘  └──────┬──────┘ └───┬──────────┘
                                  │             │
                                ▼─────┬─────────▼
                               PostgreSQL Database
                               (Consignment DB)
                                (inventory DB)
```

### Service Communication Flow

```
Gateway Request → Route by path prefix
                    ├─→ /inventory/* → Inventory Service (8081)
                    ├─→ /consignment/* → Consignment Service (8082)
                    └─→ /api-gateway/* → Gateway Service (8080)

Consignment Service → Internal Calls via OpenFeign
                    └─→ Inventory Service (for availability checks)
```

---

## 📦 Services Overview

| Service | Port | Role | Technology |
|---------|------|------|-----------|
| **Service Registry** | 8761 | Eureka server for service discovery & registration | Spring Cloud Netflix Eureka |
| **API Gateway** | 8080 | Central entry point, routing, rate limiting | Spring Cloud Gateway |
| **Inventory Service** | 8081 | Inventory availability & stock management APIs | Spring Boot + PostgreSQL |
| **Consignment Service** | 8082 | Core CMS business logic (CSRQ, CSO, CSDO, CSR, CSA, Settlement) | Spring Boot + PostgreSQL + MyBatis |



## ⚙️ Prerequisites

Before running the services, ensure you have the following installed:

| Tool | Version | Purpose |
|------|---------|---------|
| **Java (JDK)** | 17 or higher | Core runtime environment |
| **Maven** | 3.9 or higher | Build & dependency management |
| **PostgreSQL** | 14 or higher | Primary data store |
| **Git** | 2.30+ | Version control |

### Verify Installation

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check PostgreSQL (psql should be in PATH)
psql --version
```

---

## 📥 Installation & Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd consignment-module
```

### 2. Build All Services

```bash
# Full clean build (recommended first time)
mvn clean package

# Subsequent builds
mvn package
```

The build process will:
- Compile all 4 services (service-registry, api-gateway, inventory-service, consignment-service)
- Run unit tests
- Generate JAR files in `target/` folder of each service

### 3. Database Setup (PostgreSQL)

Create databases for inventory and consignment services:

```sql
-- Create inventory database
CREATE DATABASE inventory_db
  WITH
  ENCODING 'UTF8'
  LC_COLLATE='en_US.UTF-8'
  LC_CTYPE='en_US.UTF-8';

-- Create consignment database
CREATE DATABASE consignment_db
  WITH
  ENCODING 'UTF8'
  LC_COLLATE='en_US.UTF-8'
  LC_CTYPE='en_US.UTF-8';

-- Grant privileges (optional, if using specific user)
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE consignment_db TO postgres;
```

Initial schema will be created automatically via `schema.sql` on first startup (Spring Boot data initialization).

---

## 🎯 Running the Services

### Recommended Startup Order

Services must be started in this exact order to ensure proper service discovery:

#### **Step 1: Start Service Registry (Eureka)**

```bash
cd service-registry
mvn spring-boot:run
```

**Expected output:**
```
Tomcat started on port(s): 8761 (http)
Started ServiceRegistryApplication
```

**Access:** http://localhost:8761/ (Eureka Dashboard)

---

#### **Step 2: Start Inventory Service**

Open new terminal:

```bash
cd inventory-service
mvn spring-boot:run
```

**Expected output:**
```
Registering application INVENTORY-SERVICE with eureka with initial status UP
Tomcat started on port(s): 8081 (http)
Started InventoryServiceApplication
```

---

#### **Step 3: Start Consignment Service**

Open new terminal:

```bash
cd consignment-service
mvn spring-boot:run
```

**Expected output:**
```
Registering application CONSIGNMENT-SERVICE with eureka with initial status UP
Tomcat started on port(s): 8082 (http)
Started ConsignmentServiceApplication
```

---

#### **Step 4: Start API Gateway**

Open new terminal:

```bash
cd api-gateway
mvn spring-boot:run
```

**Expected output:**
```
Registering application GATEWAY with eureka with initial status UP
Tomcat started on port(s): 8080 (http)
Started ApiGatewayApplication
```

---

### Verify All Services Running

```bash
# Check Eureka Dashboard for registered services
curl -s http://localhost:8761/eureka/apps | grep -i "instance"

# Or open browser: http://localhost:8761/
# You should see 3 UP services: inventory-service, consignment-service, gateway
```



---

## 📡 API Documentation

All APIs are routed through **API Gateway (port 8080)** using the following path prefixes:
- `/inventory/api/*` → Inventory Service
- `/consignment/api/*` → Consignment Service

### Master Data Sync APIs

These endpoints synchronize master data from upstream ACMM system:

```bash
# Sync items master
POST /consignment/api/acmm/master-sync/items
Body: { items: [{ itemCode, itemName, ... }] }

# Sync item prices
POST /consignment/api/acmm/master-sync/item-prices
Body: { prices: [{ itemCode, scope, price, effectiveDate, ... }] }

# Sync suppliers
POST /consignment/api/acmm/master-sync/suppliers

# Sync contracts
POST /consignment/api/acmm/master-sync/contracts

# Sync companies
POST /consignment/api/acmm/master-sync/companies

# Sync stores
POST /consignment/api/acmm/master-sync/stores

# Sync customers
POST /consignment/api/acmm/master-sync/customers

# Sync adjustment reasons
POST /consignment/api/acmm/master-sync/reasons
```

### Consignment Setup APIs

Configure supplier and item relationships:

```bash
# Get all items available for setup
GET /consignment/api/consignment-setup/items

# Get specific item setup details
GET /consignment/api/consignment-setup/item/{itemCode}

# Add external supplier for item
POST /consignment/api/consignment-setup/item/{itemCode}/external-supplier
Body: { supplier, contract, store, price, ... }

# Update external supplier relation
PUT /consignment/api/consignment-setup/item/{itemCode}/external-supplier/{id}

# Remove external supplier relation
DELETE /consignment/api/consignment-setup/item/{itemCode}/external-supplier/{id}

# Add internal supplier for item
POST /consignment/api/consignment-setup/item/{itemCode}/internal-supplier
```

### Consignment Stock Request (CSRQ) APIs

Manage stock requests from stores:

```bash
# List all CSRQ
GET /consignment/api/csrq
Query params: ?company=COMP01&store=STORE01&status=HELD

# Get specific CSRQ
GET /consignment/api/csrq/{id}

# Create new CSRQ
POST /consignment/api/csrq
Body: {
  company: "COMP01",
  store: "STORE01",
  supplier: "SUPP01",
  contract: "CONTRACT01",
  items: [
    { itemCode: "ITEM01", quantity: 100 }
  ]
}

# Release CSRQ (HELD → RELEASED)
PUT /consignment/api/csrq/{id}/release

# Delete CSRQ (only from HELD status)
DELETE /consignment/api/csrq/{id}
```

### Consignment Stock Receive (CSRV) APIs

Track stock receipt from supplier:

```bash
# List all CSRV
GET /consignment/api/csrv
Query params: ?company=COMP01&supplier=SUPP01&status=HELD

# Get specific CSRV
GET /consignment/api/csrv/{id}

# Create new CSRV
POST /consignment/api/csrv
Body: { company, store, supplier, contract, items: [...] }

# Release CSRV (posts to supplier book value)
PUT /consignment/api/csrv/{id}/release

# Auto-create CSRV from API requests
POST /consignment/api/acmm/csrv/auto-create
```

### Customer Stock Order (CSO) APIs

Manage orders to customers:

```bash
# List all CSO
GET /consignment/api/cso
Query params: ?company=COMP01&store=STORE01&customerCode=CUST01

# Get specific CSO
GET /consignment/api/cso/{id}

# Create new CSO
POST /consignment/api/cso
Body: { company, store, customer, items: [...], autoGenerateCsdo: true }

# Release CSO (posts allocation + forecast)
PUT /consignment/api/cso/{id}/release

# Delete CSO (only HELD or ERROR status)
DELETE /consignment/api/cso/{id}

# Auto-create CSO from API
POST /consignment/api/acmm/cso/auto-create
```

### Customer Delivery Order (CSDO) APIs

Transfer from released CSO:

```bash
# List all CSDO with filters
GET /consignment/api/csdo?company=COMP01&store=STORE01&customerCode=CUST01

# Get specific CSDO
GET /consignment/api/csdo/{id}

# Transfer from CSO to CSDO
POST /consignment/api/csdo/transfer/{csoId}

# Release CSDO (removes CSO reservation, posts to customer inventory)
PUT /consignment/api/csdo/{id}/release

# Reverse released CSDO (undo inventory mutations)
PUT /consignment/api/csdo/{id}/reverse
```

### Customer Stock Return (CSR) APIs

Handle returns from customers:

```bash
# List all CSR with filters
GET /consignment/api/csr?company=COMP01&supplier=SUPP01&status=HELD

# Get specific CSR
GET /consignment/api/csr/{id}

# Create new CSR
POST /consignment/api/csr
Body: { company, store, supplier, items: [...] }

# Release CSR (triggers supplier notification)
PUT /consignment/api/csr/{id}/release

# Update actual quantities (while RELEASED)
PUT /consignment/api/csr/{id}/detail/{detailId}/actual-qty
Body: { actualQty: 50 }

# Complete CSR (deducts from supplier BV and customer consignment inventory)
PUT /consignment/api/csr/{id}/complete
```

### Customer Stock Adjustment (CSA) APIs

Adjust inventory with settlement posting:

```bash
# List all CSA
GET /consignment/api/csa?company=COMP01&transactionType=ADJ_IN

# Get specific CSA
GET /consignment/api/csa/{id}

# Create new CSA (ADJ_IN or ADJ_OUT)
POST /consignment/api/csa
Body: {
  company: "COMP01",
  transactionType: "ADJ_IN",
  settlementDecision: "UNPOST_RETURN",
  items: [...]
}

# Release CSA (processes settlement posting)
PUT /consignment/api/csa/{id}/release
```

### Settlement APIs

Generate and manage customer/supplier billing:

```bash
# List all settlements
GET /consignment/api/settlement
Query params: ?settlementType=CUSTOMER&company=COMP01

# Get specific settlement
GET /consignment/api/settlement/{id}

# Create settlement request
POST /consignment/api/settlement
Body: {
  company: "COMP01",
  store: "STORE01",
  settlementType: "CUSTOMER",
  customerCode: "CUST01"
}

# Attach documents on-demand
POST /consignment/api/settlement/{id}/details/from-documents
Body: { documents: [{docNo: "CSO001", itemCode: "ITEM01", qty: 50}] }

# Batch-generate settlements
POST /consignment/api/settlement/generate
Body: {
  company: "COMP01",
  settlementType: "SUPPLIER",
  startDate: "2024-01-01",
  endDate: "2024-01-31"
}

# Prepare settlement for billing
PUT /consignment/api/settlement/{id}/prepare-for-billing

# Mark as billed
PUT /consignment/api/settlement/{id}/mark-as-billed

# Mark as settled (payment received)
PUT /consignment/api/settlement/{id}/mark-as-settled
```

---

## ⚙️ Configuration

### Application Profiles

Each service supports multiple Spring profiles for different environments:

```bash
# Development (local with H2 in-memory DB)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Test
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
```

### Key Configuration Properties

**Consignment Service** (`consignment-service/src/main/resources/application.yml`):

```yaml
spring:
  application:
    name: consignment-service
  datasource:
    url: jdbc:postgresql://localhost:5432/consignment_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:8761/auth}

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

# Feature toggles
app:
  security:
    enabled: ${SECURITY_ENABLED:false}
  api-logging:
    enabled: ${API_LOG_ENABLED:false}
  mongodb:
    enabled: ${MONGO_ENABLED:false}
  
  settlement:
    batch:
      enabled: true
      timezone: Asia/Jakarta
      weekly-cron: "0 0 2 * * MON"
      monthly-cron: "0 0 3 1 * *"
  
  queue:
    csrq-request: csrq-request-queue
    cso-request: cso-request-queue
    settlement-batch: settlement-batch-queue
```

**Inventory Service** (`inventory-service/src/main/resources/application.yml`):

```yaml
spring:
  application:
    name: inventory-service
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_db
    username: postgres
    password: postgres

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

**API Gateway** (`api-gateway/src/main/resources/application.yml`):

```yaml
spring:
  application:
    name: gateway
  cloud:
    gateway:
      routes:
        - id: inventory-service
          uri: lb://inventory-service
          predicates:
            - Path=/inventory/**
          filters:
            - RewritePath=/inventory(?<segment>.*), $\{segment}
        
        - id: consignment-service
          uri: lb://consignment-service
          predicates:
            - Path=/consignment/**
          filters:
            - RewritePath=/consignment(?<segment>.*), $\{segment}

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### Environment Variables

```bash
# Security
export JWT_ISSUER_URI=http://localhost:8761/auth
export SECURITY_ENABLED=false

# Feature flags
export API_LOG_ENABLED=false
export MONGO_ENABLED=false

# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# AWS (for future SQS integration)
export AWS_REGION=ap-southeast-1
export AWS_ACCESS_KEY_ID=****
export AWS_SECRET_ACCESS_KEY=****
```

---

## 💾 Database Schema

### Key Tables (Consignment Service)

**consignment_item_setup**
```sql
- item_code (PK)
- item_name
- created_at
- updated_at
```

**consignment_setup_item_supplier**
```sql
- id (PK)
- item_code (FK)
- supplier_code
- contract_id
- store_code
- is_external
- created_at
```

**csrq** (Consignment Stock Request)
```sql
- id (PK)
- company
- store
- supplier
- contract
- status (HELD, RELEASED, CANCELLED, REJECTED)
- reference_no
- created_by
- created_at
```

**cso** (Customer Stock Order)
```sql
- id (PK)
- company
- store
- customer_code
- status (HELD, RELEASED, ERROR)
- auto_generate_csdo
- created_at
```

**csdo** (Customer Delivery Order)
```sql
- id (PK)
- company
- store
- cso_id (FK to CSO)
- customer_code
- status (HELD, RELEASED)
- created_at
```

**csr** (Customer Stock Return)
```sql
- id (PK)
- company
- store
- supplier_code
- status (HELD, RELEASED, COMPLETED)
- created_by
- created_at
```

**csa** (Customer Stock Adjustment)
```sql
- id (PK)
- company
- store
- transaction_type (ADJ_IN, ADJ_OUT)
- settlement_decision
- status (HELD, RELEASED)
- created_at
```

**settlement**
```sql
- id (PK)
- company
- store
- settlement_type (CUSTOMER, SUPPLIER)
- customer_code / supplier_code
- status (HELD, READY_FOR_BILLING, BILLED, SETTLED)
- total_amount
- reference_no
- created_at
```

**consignment_item_price**
```sql
- id (PK)
- item_code
- scope
- price
- effective_date
- created_at
```

---

## 🧪 Testing

### Run All Tests

```bash
# Run all unit tests
mvn test

# Run specific service tests
cd consignment-service
mvn test

# Run tests with coverage
mvn test jacoco:report
# Coverage report: target/site/jacoco/index.html
```

### Running Integration Tests

```bash
# Tests use embedded PostgreSQL (testcontainers)
mvn verify -Pintegration
```

### Test Coverage

Current test suites covering:

| Service | Coverage | Tests |
|---------|----------|-------|
| Consignment Service | 78% | 45+ tests |
| Inventory Service | 85% | 12+ tests |
| API Gateway | 60% | 8+ tests |

Key test files:
- [SettlementControllerTest.java](consignment-service/src/test/java/com/consignment/service/api/SettlementControllerTest.java)
- [SettlementServiceTest.java](consignment-service/src/test/java/com/consignment/service/service/SettlementServiceTest.java)
- [CsoServiceTest.java](consignment-service/src/test/java/com/consignment/service/service/CsoServiceTest.java)

---

## 👨‍💻 Development Guide

### Project Structure

```
consignment-module/
├── service-registry/          # Eureka server
├── api-gateway/               # Spring Cloud Gateway
├── inventory-service/         # Inventory APIs
├── consignment-service/       # Core CMS business logic
│   ├── src/main/java/
│   │   └── com/consignment/service/
│   │       ├── api/           # REST controllers
│   │       ├── domain/        # Entity models
│   │       ├── service/       # Business logic
│   │       ├── mapper/        # MyBatis data access
│   │       └── config/        # Configuration classes
│   ├── src/main/resources/
│   │   ├── mapper/            # MyBatis XML mappings
│   │   ├── schema.sql         # Database schema
│   │   └── application.yml    # Configuration
│   └── src/test/java/         # Tests
└── pom.xml                    # Maven root POM
```

### Coding Standards

1. **Package Naming:** `com.consignment.service.*`
2. **Class Naming:** 
   - Controllers: `*Controller`
   - Services: `*Service`
   - Repositories: `*Repository` or `*Mapper`
   - Entities: `*Entity` or domain objects

3. **Code Style:**
   - Use Spring Boot best practices
   - Lombok for boilerplate reduction (@Data, @Slf4j)
   - Consistent error handling with custom exceptions
   - Correlation ID propagation via MDC

4. **API Response Format:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed",
  "timestamp": "2024-01-15T10:30:00Z",
  "correlationId": "uuid-string"
}
```

### Adding a New API Endpoint

1. **Create Entity & DTO:**
```java
// Entity
@Data
@Entity
@Table(name = "my_entity")
public class MyEntity { ... }

// DTO
@Data
public class MyEntityDTO { ... }
```

2. **Create Service:**
```java
@Service
@Slf4j
public class MyEntityService {
    public MyEntityDTO create(MyEntityDTO dto) { ... }
}
```

3. **Create Controller:**
```java
@RestController
@RequestMapping("/api/my-entity")
@Slf4j
public class MyEntityController {
    @PostMapping
    public ResponseEntity<ApiResponse<MyEntityDTO>> create(@RequestBody MyEntityDTO dto) { ... }
}
```

4. **Create MyBatis Mapper (if needed):**
```xml
<!-- src/main/resources/mapper/MyEntityMapper.xml -->
<mapper namespace="com.consignment.service.mapper.MyEntityMapper">
    <insert id="insert">
        INSERT INTO my_entity (name, created_at) 
        VALUES (#{name}, NOW())
    </insert>
</mapper>
```

### Debugging

```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
mvn spring-boot:run

# Remote debugging (port 5005)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

---

## 🆘 Troubleshooting

### Service Registry Not Responding

**Problem:** Application fails to register with Eureka

**Solution:**
```bash
# Check Eureka is running
curl -s http://localhost:8761/eureka/status

# Check service logs for registration error
# Ensure eureka.client.serviceUrl.defaultZone is correct in application.yml
```

### Database Connection Failed

**Problem:** `postgresql.exceptions.PSQLException: Connection refused`

**Solution:**
```bash
# Verify PostgreSQL is running
psql -h localhost -U postgres -c "SELECT 1"

# Verify database exists
psql -h localhost -U postgres -l | grep consignment_db

# Check connection string in application.yml
# Default: jdbc:postgresql://localhost:5432/consignment_db
```

### Port Already in Use

**Problem:** `Address already in use: bind`

**Solution:**
```bash
# Kill process on port
# Windows:
netstat -ano | findstr :8082
taskkill /PID <PID> /F

# macOS/Linux:
lsof -i :8082
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8083
```

### Correlation ID Not Propagating

**Problem:** Requests lack `X-Correlation-Id` header

**Solution:**
```yaml
# Enable in API Gateway (application.yml)
app:
  correlation-id:
    enabled: true
    header-name: X-Correlation-Id
```

### MyBatis Mapper Not Found

**Problem:** `BindingException: MapperRegistry` or mapper not found

**Solution:**
1. Ensure mapper XML file is in `src/main/resources/mapper/`
2. Namespace matches fully qualified interface class
3. Add `@MapperScan` to main application class
```java
@SpringBootApplication
@MapperScan("com.consignment.service.mapper")
public class ConsignmentServiceApplication { ... }
```

---

## 📊 Sprint Roadmap

### Sprint Timeline (8 Sprints, 2 weeks each)

| Sprint | Focus Area | Status | Duration |
|--------|-----------|--------|----------|
| **1** | Foundation, Master Sync, Setup | ✅ In Progress | Week 1-2 |
| **2** | CSO & CSDO Core | ✅ In Progress | Week 3-4 |
| **3** | CSR & CSA Core | ✅ In Progress | Week 5-6 |
| **4** | Settlement Engine | 📋 Planned | Week 7-8 |
| **5** | SQS & Async Messaging | 📋 Planned | Week 9-10 |
| **6** | Reporting & Batch Jobs | 📋 Planned | Week 11-12 |
| **7** | Portal UX & JSP Pages | 📋 Planned | Week 13-14 |
| **8** | Hardening & UAT | 📋 Planned | Week 15-16 |

### Sprint 1 Features (In Progress)
- ✅ Platform foundation (security scaffold, correlation-id)
- ✅ Master data sync (ACMM inbound API)
- ✅ Consignment setup (supplier-item relations)
- ✅ CSRQ & CSRV baseline APIs
- ⏳ CSO baseline (in progress)

### Sprint 2-3 Features (In Progress)
- ✅ CSO & CSDO lifecycle
- ✅ CSR & CSA core functionality
- ✅ Inventory posting & settlement decisions
- ⏳ Edge case validations (in progress)

### Sprint 4 Features (Planned)
- 📋 Settlement compute engine
- 📋 Customer & supplier billing
- 📋 Batch generation & scheduling
- 📋 Report queries

### Sprint 5+ Features (Planned)
- 📋 SQS queue integration
- 📋 Auto-create workflows
- 📋 Portal UI
- 📋 Performance tuning

---

## 📚 Architecture Decision Records

| Decision | Rationale | Status |
|----------|-----------|--------|
| SpringBoot 3 + Java 17 | Modern LTS, strong support | ✅ Active |
| PostgreSQL + MyBatis | Familiar, performant, control | ✅ Active |
| Spring Cloud Eureka | Service discovery standard | ✅ Active |
| Correlaton ID in MDC | Request tracing across services | ✅ Active |
| Batch Settlement Scheduler | Deterministic billing cycles | ✅ Active |

---

## 🤝 Contributing

### Contribution Guidelines

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/CSO-123-add-reversal
   ```

2. **Write Tests**
   - Unit tests for services
   - Integration tests for APIs
   - Aim for >70% coverage

3. **Follow Code Standards**
   - Google Java Style Guide
   - Proper exception handling
   - Add Javadoc for public APIs

4. **Commit & Push**
   ```bash
   git commit -m "feat(cso): Add reversal capability - closes CSO-123"
   git push origin feature/CSO-123-add-reversal
   ```

5. **Create Pull Request**
   - Link to JIRA ticket
   - Describe changes & testing
   - Request review from team

---

## 📝 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

---

## 📞 Support & Contact

- **Project Lead:** Putra Irawan
- **Architecture:** Spring Boot Microservices
- **Issues:** Create GitHub Issue or JIRA ticket
- **Documentation:** See [SPRINT_1_PLAN.md](SPRINT_1_PLAN.md), [CMS_BASE_MODULE_PLAN.md](CMS_BASE_MODULE_PLAN.md)

---

## 🎯 Key Metrics

- **Total Services:** 4
- **Total Endpoints:** 40+
- **Database Tables:** 15+
- **Test Coverage:** 75%+
- **Deployment:** Docker-ready (in progress)

---

**Last Updated:** March 2026  
**Framework:** Spring Boot 3.3.5, Spring Cloud 2023.0.3  
**Database:** PostgreSQL 14+  
**Documentation:** Complete



