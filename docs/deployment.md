# Deployment

## Local Runtime Model

The local environment uses Docker Compose with:

- 3 Spring Boot services
- 3 PostgreSQL containers
- explicit environment-variable-driven configuration

Start everything with:

```bash
cp .env.example .env
docker compose up --build
```

## Ports

- `8081` user-service
- `8082` timesheet-service
- `8083` billing-service
- `3000` optional frontend
- `5433` user-db
- `5434` timesheet-db
- `5435` billing-db

## Environment Variables

### Shared Security

- `JWT_SECRET`
  - shared signing/verification secret for JWTs
- `INTERNAL_API_KEY`
  - shared key used for trusted internal service calls

### User Service Database

- `USER_DB_NAME`
- `USER_DB_USER`
- `USER_DB_PASSWORD`

### Timesheet Service Database

- `TIMESHEET_DB_NAME`
- `TIMESHEET_DB_USER`
- `TIMESHEET_DB_PASSWORD`

### Billing Service Database

- `BILLING_DB_NAME`
- `BILLING_DB_USER`
- `BILLING_DB_PASSWORD`

## Build and Test

Local build:

```bash
mvn test
```

Container image build:

```bash
docker compose build
```

## CI

GitHub Actions is configured in `.github/workflows/ci.yml`.

The pipeline:

- checks out the repository
- installs JDK 21
- caches Maven dependencies
- runs `mvn test`

Because the integration tests use `@Testcontainers(disabledWithoutDocker = true)`, the suite still passes in CI environments where Docker is unavailable, while automatically running those tests in environments that provide Docker access.

## Operational Notes

- Flyway runs automatically on application startup.
- Each service uses `ddl-auto=validate` to ensure schema drift is visible.
- Secrets are externalized through environment variables rather than hardcoded in source.
- Swagger UI is enabled for local review and interview demos.
- The optional frontend is a static Nginx-served app that proxies requests to the backend services from one origin.
