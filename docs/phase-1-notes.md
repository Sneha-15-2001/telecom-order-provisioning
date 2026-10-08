# Phase 1 Notes — Project Foundation (completed 2026-10-08)

## What was built

- Root layout `telecom-order-provisioning/` with 5 Spring Boot services, Angular app,
  per-service database scripts, and placeholders for future phases.
- Each service: Spring Boot 3.2.5 / Java 17 (`maven.compiler.release=17`, built and
  verified with JDK 17.0.20.1), Web, Validation, Data JPA, PostgreSQL driver,
  Actuator, Springdoc OpenAPI 2.5.0, JUnit 5.
- Per-service layers in Phase 1: `config` (OpenAPI, CorrelationIdFilter, CORS),
  `controller` (SystemController), `service` (SystemService), `dto`
  (SystemInfoResponse, ApiError), `exception` (GlobalExceptionHandler).
  `entity/`, `repository/`, `mapper/`, `util/` packages are scaffolded with
  `.gitkeep` and get domain code in Phases 2–6.
- Angular 22 standalone foundation (`frontend/telecom-order-ui`): dashboard shell,
  `core/api-endpoints.ts`, `X-Correlation-ID` interceptor, HttpClient wiring,
  `proxy.conf.json`. No live backend calls yet (Phase 9).
- Correlation ID: `CorrelationIdFilter` reuses or generates `X-Correlation-ID`,
  puts it in MDC `correlationId`, echoes it as a response header. Verified:
  `curl -H "X-Correlation-ID: ABC123" …/api/system/info` returns the ID in the
  header and body, and logs show `[order-service,ABC123]`.
- Logging pattern per service: `%d{…} %-5level [service,correlationId] logger - msg`,
  plus structured `service=… correlationId=… event=… status=…` messages.
- Database: 5 databases on local cluster (port 5433, see README note):
  `telecom_customer/order/inventory/provisioning/notification`.
  `database/<service>/{01_create_tables,02_indexes,03_sample_data}.sql` are
  documented placeholders until Phases 2–6.
- Explicitly NOT built (per order): Kafka, Docker, AI investigator/RCA/fix engines,
  domain APIs beyond the foundation contract.

## Verification (all on 2026-10-08)

| Check | Result |
|---|---|
| `mvn -DskipTests clean compile` × 5 (JDK 17) | BUILD SUCCESS × 5 |
| `mvn test` × 5 (against real PostgreSQL) | 1/1 pass × 5, BUILD SUCCESS × 5 |
| `npm run build` (Angular) | success, `dist/telecom-order-ui` |
| All 5 jars start independently (8081–8085) | UP, `db: UP (PostgreSQL)` on all |
| `/actuator/health` × 5 | `{"status":"UP",…}` |
| `/api/system/ping` | `{"status":"UP","service":"…"}` |
| `/api/system/info` + `X-Correlation-ID: ABC123` | ID echoed in header + body |
| `/v3/api-docs` titles | Customer/Order/Inventory/Provisioning/Notification Service API |
| `/swagger-ui/index.html` | 302 → 200 after redirect (verified on 8081) |
| Correlation log line | `[order-service,ABC123] … event=SYSTEM_INFO status=SUCCESS` |

## How to run (this machine)

```bash
# 1. database (dedicated cluster on 5433)
/Library/PostgreSQL/18/bin/pg_ctl -D ~/.local/share/telecom-pg -l ~/.local/share/telecom-pg.log start
export POSTGRES_HOST=localhost POSTGRES_PORT=5433 POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres

# 2. backends (one terminal each, JDK 17)
/opt/homebrew/opt/openjdk@17/bin/java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar
# … or: cd customer-service && mvn spring-boot:run   (repeat per service)

# 3. frontend
cd frontend/telecom-order-ui && npm install && npm start   # http://localhost:4200
```

## Services left RUNNING after verification

PIDs 7128–7132 on ports 8081–8085 (logs in /tmp/cust.log, /tmp/order.log,
/tmp/inv.log, /tmp/prov.log, /tmp/notif.log). Stop with `kill 7128 7129 7130 7131 7132`.
