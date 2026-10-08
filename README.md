# Telecom Order Provisioning System

A **brand-new, standalone** telecom order-provisioning demo built from scratch.
It is NOT derived from any enterprise codebase and connects to NO real
telecom/network infrastructure — provisioning and SMS are simulated.

> **New here? Start with [`SUMMARY.md`](SUMMARY.md)** — the project told as one
> customer's story. Then [`OVERVIEW.md`](OVERVIEW.md) (what each piece does)
> and [`SETUP.md`](SETUP.md) (run it yourself in ~15 minutes).

## What it does

Customer signs up → places an order → pays → system reserves a real unit
(SIM / number / fiber port) → activates it → sends an SMS → order COMPLETED
with a full timeline. One **Fulfill end-to-end** call runs reserve → provision →
complete → notify as a saga with automatic compensation on failure.

## Pieces

| Piece | Owns | Port | Swagger |
|---|---|---|---|
| customer-service | People, addresses, subscriptions, eligibility | 8081 | [swagger](http://localhost:8081/swagger-ui/index.html) |
| order-service | Orders, payments, promos, fulfill saga | 8082 | [swagger](http://localhost:8082/swagger-ui/index.html) |
| inventory-service | SIMs, numbers, devices, fiber ports, holds | 8083 | [swagger](http://localhost:8083/swagger-ui/index.html) |
| provisioning-service | Simulated activation | 8084 | [swagger](http://localhost:8084/swagger-ui/index.html) |
| notification-service | Simulated SMS/email + templates | 8085 | [swagger](http://localhost:8085/swagger-ui/index.html) |
| telecom-order-ui | NexaTel storefront (dark/light mode) | 4200 | — |

128 APIs total ([full inventory](docs/api-overview.md)). Each backend owns its
PostgreSQL database; services talk over REST with `X-Correlation-ID` tracing.

## Technology

Java 17 · Spring Boot 3.2 · Spring Data JPA · PostgreSQL · WebClient ·
Angular 22 · JUnit 5 + Mockito · Springdoc OpenAPI.
Kafka (Phase 19) and Docker (Phase 20) are intentionally **not here yet**.

## Quick start

```bash
# databases (once) — see SETUP.md for the full guide
psql -h localhost -U postgres -c "CREATE DATABASE telecom_customer;"
# … + telecom_order, telecom_inventory, telecom_provisioning, telecom_notification

export POSTGRES_HOST=localhost POSTGRES_PORT=5433 POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres

# one terminal per service, from the repo root
cd customer-service && mvn spring-boot:run   # :8081 (repeat for order :8082, inventory :8083, provisioning :8084, notification :8085)

# website
cd frontend/telecom-order-ui && npm install && npm start   # :4200
```

Health: `curl localhost:808{1..5}/actuator/health` → all `UP`.

## Current state (Phases 1–13 done)

5 domain microservices · REST integration · fulfill saga · NexaTel UI (10 routes) ·
94 backend tests + specs green · rolling file logs with correlation tracing ·
**10 reproducible incident scenarios** (`incident-scenarios/`, Jira-style cards).

Remaining: logging hardening → incidents → **AI investigator + RCA + fix
recommenders (14–17)** → demo → Kafka → Docker. Roadmap: `docs/architecture.md`.

## Docs

- [`SUMMARY.md`](SUMMARY.md) — the story version
- [`OVERVIEW.md`](OVERVIEW.md) — plain-English guide for new devs
- [`SETUP.md`](SETUP.md) — install → run → build → test
- [`docs/testing.md`](docs/testing.md) — test runbook · [`docs/api-overview.md`](docs/api-overview.md) — all 128 APIs
- [`docs/phase-N-notes.md`](docs/) — what each phase built and how it was verified
