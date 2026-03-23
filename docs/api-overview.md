# API Overview

## Authentication Model

All business endpoints are protected with Bearer JWT except:

- `POST /api/v1/auth/login`
- Swagger/OpenAPI endpoints
- internal service endpoints guarded by `X-Internal-Api-Key`

## User Service

Base URL: `http://localhost:8081`

### Auth

- `POST /api/v1/auth/login`

### Users

- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`

### Consultants

- `POST /api/v1/consultants`
- `GET /api/v1/consultants`
- `GET /api/v1/consultants/{id}`
- `PUT /api/v1/consultants/{id}`

### Clients

- `POST /api/v1/clients`
- `GET /api/v1/clients`
- `GET /api/v1/clients/{id}`
- `PUT /api/v1/clients/{id}`

### Projects

- `POST /api/v1/projects`
- `GET /api/v1/projects`
- `GET /api/v1/projects/{id}`
- `PUT /api/v1/projects/{id}`

### Assignments

- `POST /api/v1/assignments`
- `GET /api/v1/assignments`
- `GET /api/v1/assignments/{id}`

### Audit

- `GET /api/v1/audit`

### Internal

- `GET /internal/v1/assignments/validate`

## Timesheet Service

Base URL: `http://localhost:8082`

### Time Entries

- `POST /api/v1/time-entries`
- `GET /api/v1/time-entries`
- `GET /api/v1/time-entries/{id}`
- `PUT /api/v1/time-entries/{id}`
- `POST /api/v1/time-entries/{id}/submit`
- `POST /api/v1/time-entries/{id}/approve`
- `POST /api/v1/time-entries/{id}/reject`

### Reports

- `GET /api/v1/reports/time-entries`

### Audit

- `GET /api/v1/audit/time-entries`

### Internal

- `GET /internal/v1/time-entries/approved`

## Billing Service

Base URL: `http://localhost:8083`

### Billing Generation

- `POST /api/v1/billing/generate`

### Billing Summaries

- `GET /api/v1/billing/summaries`
- `GET /api/v1/billing/summaries/{id}`

### Billing Periods

- `GET /api/v1/billing-periods`

### Audit

- `GET /api/v1/audit/billing`

## Example Payloads

### Login Request

```json
{
  "email": "admin@consultingops.local",
  "password": "Admin123!"
}
```

### Create Consultant

```json
{
  "fullName": "Nadia Fischer",
  "email": "nadia.fischer@consultingops.local",
  "password": "Consultant123!",
  "employeeCode": "EMP-1102",
  "jobTitle": "Java Backend Consultant",
  "seniorityLevel": "SENIOR",
  "hourlyRate": 125.00,
  "status": "ACTIVE"
}
```

### Create Project Assignment

```json
{
  "consultantId": "4c3a9435-44c4-4e7d-9d9e-6d772c77f7ce",
  "projectId": "73c9a5f7-1ec3-4e1f-8ad8-8f1d6cc3a417",
  "assignedRole": "Backend Lead",
  "allocationPercentage": 80,
  "startDate": "2026-03-01",
  "endDate": "2026-06-30",
  "active": true
}
```

### Create Time Entry

```json
{
  "consultantId": "4c3a9435-44c4-4e7d-9d9e-6d772c77f7ce",
  "projectId": "73c9a5f7-1ec3-4e1f-8ad8-8f1d6cc3a417",
  "workDate": "2026-03-20",
  "hours": 8.0,
  "description": "Implemented billing aggregation and timesheet reporting endpoints",
  "billable": true
}
```

### Reject Time Entry

```json
{
  "reason": "Hours need clearer delivery notes before approval"
}
```

### Generate Billing

```json
{
  "clientId": "0f9e3fb1-0285-455a-92cb-fec2cbb151f3",
  "projectId": "73c9a5f7-1ec3-4e1f-8ad8-8f1d6cc3a417",
  "startDate": "2026-03-01",
  "endDate": "2026-03-31",
  "currency": "EUR"
}
```
