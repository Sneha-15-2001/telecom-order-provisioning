# Telecom Order Provisioning System — What Is This?

A **practice project** that works like the backend of a mobile/broadband company
(think Airtel or Jio): customers sign up, place orders for SIMs / phones / fiber,
and the system reserves stock, activates the service, and sends an SMS.

> Built from scratch for learning and hackathons. It talks to **no real**
> telecom network — provisioning and SMS are simulated.

## What it does (one story)

1. **Customer** signs up (name, email, phone).
2. **Order** is created for a plan, phone, or broadband package.
3. Order is **validated** (is the customer eligible?) and **submitted**.
4. Customer **pays** (simulated payment check).
5. **Inventory** reserves a real unit — a SIM, a mobile number, a fiber port.
6. **Provisioning** "activates" it (simulated network activation).
7. **Notification** sends the customer an SMS/email (simulated).
8. Order shows **COMPLETED** with a full timeline.

One button — **Fulfill end-to-end** — runs steps 5–8 automatically, and rolls
everything back cleanly if any step fails.

## The pieces (5 small backends + 1 website)

| Piece | What it owns | Address |
|---|---|---|
| customer-service | People, addresses, subscriptions | :8081 |
| order-service | Orders, payments, promos, the fulfill saga | :8082 |
| inventory-service | SIMs, numbers, devices, fiber ports, holds | :8083 |
| provisioning-service | Simulated activation | :8084 |
| notification-service | Simulated SMS/email + templates | :8085 |
| telecom-order-ui | The NexaTel website (dark/light mode) | :4200 |

Each backend has **its own database** and talks to the others only over HTTP
(`X-Correlation-ID` follows every request, so one ID traces a whole order).

## What you need (the short list)

- **Java 17**, **Maven**, **Node 20+**, **PostgreSQL**
- That's it. No Kafka, no Docker yet — those are the final phases.

## Where things live

- `customer-service/`, `order-service/`, … — one folder per backend
- `frontend/telecom-order-ui/` — the website
- `database/<service>/` — table scripts + sample data
- `incident-scenarios/` — 10 Jira-style bug stories you can reproduce
- `docs/` — detailed notes per phase
- **`SETUP.md`** — step-by-step: install → database → run everything

## For the curious: what's next in this project

Incident scenarios → an AI that investigates production bugs from logs →
Kafka events → Docker packaging. See `docs/architecture.md` for the full roadmap.
