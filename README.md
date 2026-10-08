# Telecom Order Provisioning System (Hackathon / Portfolio Project)

A **brand-new, standalone** telecom order-provisioning demo built from scratch for a hackathon.
It is NOT derived from any enterprise codebase and connects to NO real telecom/network infrastructure.

## Phase 1 — Project Foundation (current phase)

What exists in Phase 1:

- Root project structure (`telecom-order-provisioning/`)
- Five independently runnable Spring Boot 3.x / Java 17 microservices
- Angular frontend foundation (`frontend/telecom-order-ui`, port 4200)
- PostgreSQL configuration (one database per service, no cross-service table access)
- Actuator health for every service
- Swagger/OpenAPI for every service
- Basic structured logging foundation
- `X-Correlation-ID` propagation foundation (critical for the future AI investigator)
- Database folder structure (`database/<service>/`)
- Placeholder folders for future phases (`incident-scenarios/`, `sample-logs/`, `ai-investigator/`, `docs/`)

What is **explicitly NOT** in Phase 1 (per the build order):

- ❌ Kafka (Phase 19 only)
- ❌ Docker (Phase 20 only)
- ❌ AI investigator / RCA / data-fix / code-fix engines (Phases 14–17)
- ❌ Full ~100 APIs (Phase 8)
- ❌ Complete Angular screens (Phase 9)
- ❌ Production incident scenarios (Phase 13)

## Technology Stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 17, Spring Boot 3.2.x, Spring Web, Spring Data JPA, Hibernate, Jakarta Bean Validation, Spring Boot Actuator, Springdoc OpenAPI 2.x, Maven, JUnit 5, Mockito |
| Frontend | Angular (standalone), TypeScript, Angular Router, HttpClient, Reactive Forms |
| Database | PostgreSQL (one DB per service) |
| Future   | Apache Kafka (Phase 19), Docker + Docker Compose (Phase 20) |

## Microservices & Ports

| Service             | Port | Database              | Health                          | Swagger                          |
|---------------------|------|-----------------------|---------------------------------|----------------------------------|
| customer-service     | 8081 | `telecom_customer`     | http://localhost:8081/actuator/health | http://localhost:8081/swagger-ui/index.html |
| order-service        | 8082 | `telecom_order`        | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui/index.html |
| inventory-service    | 8083 | `telecom_inventory`    | http://localhost:8083/actuator/health | http://localhost:8083/swagger-ui/index.html |
| provisioning-service | 8084 | `telecom_provisioning` | http://localhost:8084/actuator/health | http://localhost:8084/swagger-ui/index.html |
| notification-service | 8085 | `telecom_notification` | http://localhost:8085/actuator/health | http://localhost:8085/swagger-ui/index.html |

Angular UI: http://localhost:4200

## Prerequisites

- Java 17 (`java -version` → 17.x; this repo targets `maven.compiler.release=17`)
- Maven 3.9+
- Node 20+ and npm 10+
- PostgreSQL 14+ running on `localhost:5432` with user `postgres` (password `postgres` by default; override via `.env` / environment variables)

## PostgreSQL Setup

```bash
# create the five service databases (run once)
psql -h localhost -U postgres -c "CREATE DATABASE telecom_customer;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_order;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_inventory;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_provisioning;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_notification;"

# optional: inspect provided SQL placeholders
ls database/customer database/order database/inventory database/provisioning database/notification
```

Each service's JPA config uses `ddl-auto: update` in Phase 1 so the service starts cleanly on an empty database.
Strict `validate` + real tables arrive with Phase 2+ entities and `database/<service>/01_create_tables.sql`.

> Local note (this machine): the pre-installed PostgreSQL on port 5432 was not
> accessible, so Phase 1 verification uses a dedicated local cluster on port
> 5433 (data dir `~/.local/share/telecom-pg`). Start it with:
>
> ```bash
> /Library/PostgreSQL/18/bin/pg_ctl -D ~/.local/share/telecom-pg -l ~/.local/share/telecom-pg.log start
> ```
>
> and export `POSTGRES_PORT=5433` (plus `POSTGRES_HOST=localhost`,
> `POSTGRES_USER=postgres`, `POSTGRES_PASSWORD=postgres`) before `mvn` commands.
> If you have access to the :5432 instance, just create the five databases there
> and use the defaults — no code change needed. See `docs/phase-1-notes.md`.

## How to Start Each Service (Phase 1)

From the repo root, in five separate terminals:

```bash
# Terminal 1
cd customer-service && mvn spring-boot:run
# Terminal 2
cd order-service && mvn spring-boot:run
# Terminal 3
cd inventory-service && mvn spring-boot:run
# Terminal 4
cd provisioning-service && mvn spring-boot:run
# Terminal 5
cd notification-service && mvn spring-boot:run
```

Or build once and run the jars:

```bash
for s in customer-service order-service inventory-service provisioning-service notification-service; do
  (cd $s && mvn -q clean package -DskipTests)
done
java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar
```

## How to Start Angular (Phase 1)

```bash
cd frontend/telecom-order-ui
npm install
npm start
# open http://localhost:4200
```

## Phase 1 Smoke Test

```bash
# health (Actuator)
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health

# foundation APIs (each service exposes the same Phase-1 contract)
curl http://localhost:8081/api/system/ping
curl http://localhost:8081/api/system/info
curl -H "X-Correlation-ID: ABC123" http://localhost:8081/api/system/info -i

# OpenAPI docs
curl http://localhost:8081/v3/api-docs | head -c 500
```

Expected `ping` response:

```json
{ "status": "UP", "service": "customer-service" }
```

The `X-Correlation-ID` you send is echoed back as a response header and included in logs —
this is the foundation the AI investigator (Phase 14) will use to correlate requests.

## Logging (Phase 1 foundation)

Console pattern includes `service`, `correlationId`, operation and status, e.g.:

```text
2026-10-08T10:15:30.123 INFO  [customer-service,ABC123] ... event=SYSTEM_PING status=SUCCESS
```

Full JSON/structured logging and per-order correlation arrive in Phase 12. Phase 1 only
establishes MDC + filter + pattern so every later log line can be traced.

## Project Structure (Phase 1)

```text
telecom-order-provisioning/
├── customer-service/          # Spring Boot, port 8081
├── order-service/             # Spring Boot, port 8082
├── inventory-service/         # Spring Boot, port 8083
├── provisioning-service/      # Spring Boot, port 8084
├── notification-service/      # Spring Boot, port 8085
├── frontend/telecom-order-ui/ # Angular, port 4200
├── database/
│   ├── customer/              # 01_create_tables.sql, 02_indexes.sql, 03_sample_data.sql (placeholders in P1)
│   ├── order/
│   ├── inventory/
│   ├── provisioning/
│   └── notification/
├── incident-scenarios/        # Phase 13 (placeholder)
├── sample-logs/               # Phase 12-13 (placeholder)
├── ai-investigator/           # Phases 14-17 (placeholder, no code in P1)
├── docs/                      # architecture + phase notes
├── .gitignore
├── .env.example
└── README.md
```

## REST Communication Decision (for Phase 7, recorded now)

**Decision: use Spring `RestClient`/`WebClient` in Phase 7, NOT OpenFeign, and NOT Kafka.**

- Phase 7 will use synchronous REST (`Order → Customer/Inventory/Provisioning/Notification`).
- Kafka is forbidden until Phase 19, after the AI investigator + demo work over REST.
- The exact client (WebClient with `X-Correlation-ID` propagation filter) will be implemented in Phase 7.

## Documentation

- `docs/architecture.md` — service responsibilities, ports, DB ownership, build order
- `docs/api-overview.md` — full API inventory (128 ops as of Phase 11)
- `docs/testing.md` — how to run backend + frontend tests and manual verification
- `docs/phase-N-notes.md` — per-phase build + verification notes (1–11 so far)

## Current state (end of Phase 11)

Phases 1–11 complete: 5 domain microservices (128 APIs), WebClient REST integration
with correlation propagation, end-to-end `fulfill` saga with compensation, NexaTel
Angular UI (10 routes, dark/light mode), 94 backend tests + 2 frontend specs green.
Remaining: 12 logging → 13 incidents → 14–17 AI investigator stack → 18 demo →
19 Kafka → 20 Docker.
