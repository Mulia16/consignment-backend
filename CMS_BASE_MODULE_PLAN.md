# CMS Base Module Plan

## Scope

This base module focuses on core Consignment Management Service (CMS) request lifecycle APIs and internal architecture.

## Phase 1 (Completed in this iteration)

- Domain model and status lifecycle
  - Status enum: HELD, UPDATED, CANCELLED, REJECTED
  - Request aggregate object with timestamps
- Repository abstraction
  - In-memory repository for quick iteration
- Service use cases
  - Create consignment request
  - Get by ID
  - List all requests
  - Update request status with transition guards
- API layer
  - POST /api/v1/consignments/request
  - GET /api/v1/consignments
  - GET /api/v1/consignments/{requestId}
  - PATCH /api/v1/consignments/{requestId}/status
- Error handling
  - 404 not found
  - 400 invalid transition / validation error

## Phase 2 (Next)

- Persistence and migrations
  - Replace in-memory repository with PostgreSQL + Spring Data JPA
  - Add migration scripts (Flyway)
- Better API contracts
  - OpenAPI/Swagger docs
  - Request/response versioning strategy
- Validation rules
  - Store/supplier relationship checks based on setup module
  - Business date and cutoff checks

## Phase 3

- Workflow and async integration
  - Event publishing for request created/updated
  - Retry/idempotency for inventory reservation and external APIs
- Security and tenancy
  - JWT auth and role checks
  - Company/store scoped data access

## Phase 4

- Operational readiness
  - Structured logging and tracing
  - Metrics dashboards and alerts
  - Contract/integration tests in CI
