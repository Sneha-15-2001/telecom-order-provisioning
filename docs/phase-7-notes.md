# Phase 7 Notes — REST service-to-service communication (completed 2026-10-08)

Decision: **WebClient** (not OpenFeign) — no extra deps beyond
spring-boot-starter-webflux, ExchangeFilterFunction propagation, Spring's
recommended client, non-blocking-ready. Kafka stays out until Phase 19.

Built in order-service: `integration/` package — CorrelationPropagationFilter
(MDC→header, generates when absent), WebClientConfig (4 beans, 5s timeout,
URLs via ${…_URL} env), wire DTOs (String statuses, no enum coupling),
4 clients (customer/inventory/provisioning/notification).
Wired: OrderService.validate now calls customer-service; invalid/404/downstream-down
→ order FAILED with reason (CUSTOMER_INVALID / CUSTOMER_NOT_FOUND /
CUSTOMER_SERVICE_UNAVAILABLE), never a 500.

Verified: 17/17 tests (incl. JDK-HttpServer stub test asserting X-Correlation-ID
on all 4 clients + 404 mapping); live: order validate → customer-service,
same correlation ID in both services' logs
(order-service CUSTOMER_VALIDATE_CALL ↔ customer-service CUSTOMER_VALIDATED).
Inventory/provisioning/notification clients implemented + stub-tested; exercised
live by fulfillment orchestration in Phase 10.
