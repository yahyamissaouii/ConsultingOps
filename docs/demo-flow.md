# Demo Flow

This flow demonstrates the full business lifecycle:

1. create consultant
2. create client
3. create project
4. assign consultant
5. log hours
6. approve hours
7. generate billing summary

## 1. Login as Admin

```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@consultingops.local","password":"Admin123!"}' | jq -r '.accessToken')
```

## 2. Create Consultant

```bash
CONSULTANT=$(curl -s -X POST http://localhost:8081/api/v1/consultants \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nadia Fischer",
    "email": "nadia.fischer@consultingops.local",
    "password": "Consultant123!",
    "employeeCode": "EMP-1102",
    "jobTitle": "Java Backend Consultant",
    "seniorityLevel": "SENIOR",
    "hourlyRate": 125.00,
    "status": "ACTIVE"
  }')

CONSULTANT_ID=$(echo "${CONSULTANT}" | jq -r '.id')
```

## 3. Create Client

```bash
CLIENT=$(curl -s -X POST http://localhost:8081/api/v1/clients \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "BluePeak Manufacturing",
    "contactEmail": "finance@bluepeak.example",
    "billingAddress": "220 Market Square, Berlin",
    "taxIdentifier": "BP-2026-001",
    "active": true
  }')

CLIENT_ID=$(echo "${CLIENT}" | jq -r '.id')
```

## 4. Create Project

```bash
PROJECT=$(curl -s -X POST http://localhost:8081/api/v1/projects \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"code\": \"BPM-ERP-01\",
    \"name\": \"ERP Modernization\",
    \"description\": \"Back-office modernization for finance and operations\",
    \"clientId\": \"${CLIENT_ID}\",
    \"startDate\": \"2026-03-01\",
    \"endDate\": \"2026-06-30\",
    \"billingModel\": \"TIME_AND_MATERIALS\",
    \"status\": \"ACTIVE\"
  }")

PROJECT_ID=$(echo "${PROJECT}" | jq -r '.id')
```

## 5. Assign Consultant

```bash
curl -s -X POST http://localhost:8081/api/v1/assignments \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"consultantId\": \"${CONSULTANT_ID}\",
    \"projectId\": \"${PROJECT_ID}\",
    \"assignedRole\": \"Backend Lead\",
    \"allocationPercentage\": 100,
    \"startDate\": \"2026-03-01\",
    \"endDate\": \"2026-06-30\",
    \"active\": true
  }"
```

## 6. Login as Consultant

```bash
CONSULTANT_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nadia.fischer@consultingops.local","password":"Consultant123!"}' | jq -r '.accessToken')
```

## 7. Log Hours

```bash
ENTRY=$(curl -s -X POST http://localhost:8082/api/v1/time-entries \
  -H "Authorization: Bearer ${CONSULTANT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"consultantId\": \"${CONSULTANT_ID}\",
    \"projectId\": \"${PROJECT_ID}\",
    \"workDate\": \"2026-03-20\",
    \"hours\": 8.0,
    \"description\": \"Implemented billing aggregation endpoint and approval reporting\",
    \"billable\": true
  }")

ENTRY_ID=$(echo "${ENTRY}" | jq -r '.id')
```

## 8. Submit Hours

```bash
curl -s -X POST http://localhost:8082/api/v1/time-entries/${ENTRY_ID}/submit \
  -H "Authorization: Bearer ${CONSULTANT_TOKEN}"
```

## 9. Login as Manager

```bash
MANAGER_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@consultingops.local","password":"Manager123!"}' | jq -r '.accessToken')
```

## 10. Approve Hours

```bash
curl -s -X POST http://localhost:8082/api/v1/time-entries/${ENTRY_ID}/approve \
  -H "Authorization: Bearer ${MANAGER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"note":"Delivery notes verified"}'
```

## 11. Login as Billing Admin

```bash
BILLING_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"billing@consultingops.local","password":"Billing123!"}' | jq -r '.accessToken')
```

## 12. Generate Billing Summary

```bash
curl -s -X POST http://localhost:8083/api/v1/billing/generate \
  -H "Authorization: Bearer ${BILLING_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"clientId\": \"${CLIENT_ID}\",
    \"projectId\": \"${PROJECT_ID}\",
    \"startDate\": \"2026-03-01\",
    \"endDate\": \"2026-03-31\",
    \"currency\": \"EUR\"
  }"
```

## 13. Review Billing Summaries

```bash
curl -s "http://localhost:8083/api/v1/billing/summaries?clientId=${CLIENT_ID}" \
  -H "Authorization: Bearer ${BILLING_TOKEN}"
```
