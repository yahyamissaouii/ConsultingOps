# ConsultingOps

ConsultingOps is a production-style Java microservices portfolio project for internal consulting operations. It models the core workflow of a consulting company: managing consultants and project assignments, capturing time, running approval workflows, and generating invoice-ready billing summaries from approved work.

The repository is designed to demonstrate backend engineering depth rather than surface CRUD. It includes bounded service ownership, JWT-based security, service-to-service communication, database migrations, audit logging, Docker Compose startup, OpenAPI docs, and targeted tests.

## Business Problem

Consulting firms need an internal platform that can:

- manage internal users, consultants, clients, and projects
- assign consultants to delivery work with allocation constraints
- capture billable and non-billable time against valid assignments
- enforce draft, submission, approval, and rejection workflow rules
- generate persisted billing summaries from approved time entries
- expose traceable audit history for operational and financial actions

## Architecture Summary

The system is implemented as a three-service monorepo:

- `user-service`
  - owns users, consultants, clients, projects, project assignments, login, and JWT issuance
  - exposes internal validation endpoints used by downstream services
- `timesheet-service`
  - owns time entries, workflow transitions, reporting, and timesheet audit history
  - validates consultant/project references through `user-service`
  - stores snapshots required for stable downstream billing
- `billing-service`
  - consumes approved entries from `timesheet-service`
  - persists billing periods and invoice-ready summaries
  - records billing audit events
- `optional-frontend`
  - static single-page internal console served by Nginx
  - proxies backend requests to all three services from one origin
  - supports login, directory browsing, basic timesheet actions, and billing generation

Each service has:

- its own Spring Boot application
- its own PostgreSQL schema/database
- its own Flyway migrations
- isolated package structure and persistence layer
- individual OpenAPI / Swagger UI endpoint

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- OpenFeign
- PostgreSQL
- Flyway
- Bean Validation
- OpenAPI / Swagger
- Docker + Docker Compose
- JUnit 5
- Mockito
- Testcontainers

## Repository Layout

```text
consultingops/
  README.md
  AGENTS.md
  pom.xml
  docker-compose.yml
  .env.example
  docs/
    architecture.md
    api-overview.md
    deployment.md
    demo-flow.md
  user-service/
  timesheet-service/
  billing-service/
  optional-frontend/
  .github/workflows/ci.yml
```

## Local Setup

1. Create a local environment file:

```bash
cp .env.example .env
```

2. Run the full stack with Docker Compose:

```bash
docker compose up --build
```

3. Open Swagger UIs:

- User Service: `http://localhost:8081/swagger-ui.html`
- Timesheet Service: `http://localhost:8082/swagger-ui.html`
- Billing Service: `http://localhost:8083/swagger-ui.html`

4. Open the frontend:

- Frontend App: `http://localhost:3000`

## Seeded Demo Accounts

`user-service` seeds demo identities on first startup:

- `admin@consultingops.local` / `Admin123!`
- `manager@consultingops.local` / `Manager123!`
- `billing@consultingops.local` / `Billing123!`
- `consultant@consultingops.local` / `Consultant123!`

It also seeds one active consultant, one client, one project, and one assignment so the end-to-end workflow is easy to demo.

## Running Tests

Run the full suite:

```bash
mvn test
```

Notes:

- unit tests run by default across all services
- Testcontainers-backed integration tests are included
- those integration tests automatically skip when Docker is unavailable in the execution environment

## Example Workflow

The standard demo flow is:

1. Login as admin or manager
2. Create a consultant or use the seeded consultant
3. Create a client
4. Create a project
5. Assign the consultant
6. Login as consultant and create / submit time entries
7. Login as manager and approve entries
8. Login as billing admin and generate billing summaries

Full request examples are documented in [docs/demo-flow.md](/home/yahya/Desktop/ConsultingOPS/docs/demo-flow.md).

## Sample API Usage

Login:

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@consultingops.local","password":"Admin123!"}'
```

Create a time entry:

```bash
curl -X POST http://localhost:8082/api/v1/time-entries \
  -H "Authorization: Bearer <consultant-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "consultantId":"<consultant-id>",
    "projectId":"<project-id>",
    "workDate":"2026-03-20",
    "hours":8.0,
    "description":"Implemented billing aggregation logic",
    "billable":true
  }'
```

Generate billing:

```bash
curl -X POST http://localhost:8083/api/v1/billing/generate \
  -H "Authorization: Bearer <billing-admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId":"<client-id>",
    "startDate":"2026-03-01",
    "endDate":"2026-03-31",
    "currency":"EUR"
  }'
```

## Key Design Decisions

- JWT issuance is centralized in `user-service`, while downstream services validate the token locally with the shared secret.
- `timesheet-service` validates project/consultant assignments through `user-service` and stores immutable snapshots required for stable billing calculations.
- `billing-service` is intentionally not a second source of truth for operational entities; it only consumes approved time and persists financial read models.
- Audit data is kept close to the service that owns the business action.

## Future Improvements

- Spring Cloud Gateway for unified entry and route-level policy enforcement
- event-driven notifications for approval and rejection actions
- CSV / PDF export for billing summaries
- optimistic locking for high-concurrency operational updates
- richer role-specific admin frontend beyond the current lightweight static console
