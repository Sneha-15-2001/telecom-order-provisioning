# Architecture (Phase 1)

## Services

Five independent Spring Boot services. Each owns its own PostgreSQL database.
No direct cross-database access. Before Kafka (Phase 19), all inter-service
communication is synchronous REST with `X-Correlation-ID` propagation (Phase 7).

```text
Angular (4200)
   │ REST
   ▼
order-service (8082) ──REST──▶ customer-service (8081)
                     ──REST──▶ inventory-service (8083)
                     ──REST──▶ provisioning-service (8084)
                     ──REST──▶ notification-service (8085)
```

## Service responsibilities (summary)

1. **customer-service (8081, telecom_customer)** — onboarding, profile, address,
   status (ACTIVE/INACTIVE/SUSPENDED/BLOCKED), eligibility, subscriptions, history.
2. **order-service (8082, telecom_order)** — orchestration: create/validate/submit/
   track/modify/cancel/retry, payments, promotions, bulk/corporate orders.
3. **inventory-service (8083, telecom_inventory)** — SIM/eSIM/MSISDN/device/IMEI/
   fiber-port/ONT/router resources; reserve/allocate/release/reconcile/quarantine.
4. **provisioning-service (8084, telecom_provisioning)** — simulated network
   activation (mobile/eSIM/broadband/roaming), suspend/resume/deactivate, rollback.
5. **notification-service (8085, telecom_notification)** — simulated SMS/email
   delivery, templates, retry. No real providers in early phases.

## Data ownership

| Service | Database | May access |
|---------|----------|------------|
| customer | telecom_customer | only its own tables |
| order | telecom_order | only its own tables |
| inventory | telecom_inventory | only its own tables |
| provisioning | telecom_provisioning | only its own tables |
| notification | telecom_notification | only its own tables |

Cross-service reads go through REST (Phase 7) and later events (Phase 19).

## Cross-cutting concerns (Phase 1 foundation)

- **Actuator**: `health`, `info`, `metrics` exposed per service.
- **Swagger**: Springdoc OpenAPI at `/swagger-ui/index.html` + `/v3/api-docs`.
- **DTOs**: entities are never exposed; Phase 1 ships `SystemInfoResponse` + `ApiError` as the pattern.
- **Validation**: Jakarta Bean Validation on all request DTOs from Phase 2 on.
- **Errors**: `@RestControllerAdvice` → `{timestamp,status,error,message,path}`.
- **Logging**: MDC `correlationId` + `service` in every line; JSON/structured logging in Phase 12.
- **Correlation ID**: `X-Correlation-ID` filter generates/propagates IDs; downstream propagation in Phase 7.

## Build order (reminder)

P1 foundation → P2 customer → P3 order → P4 inventory → P5 provisioning →
P6 notification → P7 REST → P8 ~100 APIs → P9 Angular → P10 workflows →
P11 testing/swagger/docs → P12 logging → P13 incidents → P14 investigator →
P15 RCA → P16 data-fix → P17 code-fix → P18 demo → P19 Kafka → P20 Docker.
