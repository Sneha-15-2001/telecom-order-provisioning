# API Overview (Phase 11 audit — 128 operations, 0 filler)

Target met: ~20+ meaningful operations per service, ~100+ total, no filler.
Counts from live `/v3/api-docs` on 2026-10-08 (all services UP).

| Service | Paths | Ops |
|---|---|---|
| customer-service (8081) | 19 | 25 |
| order-service (8082) | 25 | 29 |
| inventory-service (8083) | 24 | 27 |
| provisioning-service (8084) | 23 | 25 |
| notification-service (8085) | 19 | 22 |
| **Total** | **110** | **128** |

## Customer Service (8081) — 25 ops

Customers: `POST /api/customers`, `GET` (paged + status), `GET /search`,
`GET /{id}`, `GET /number/{n}`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/status`,
`POST /{id}/suspend|reactivate|validate`, `GET /{id}/eligibility|history`,
`GET+POST /{id}/addresses`, `GET+POST /{id}/subscriptions`,
`PUT /{id}/subscriptions/{s}`, `POST /{id}/subscriptions/{s}/cancel`.
Corporate: `POST+GET /api/corporate-accounts`, `GET /{id}`,
`POST /{id}/customers/{cid}`. System: `GET /api/system/ping|info`.

## Order Service (8082) — 28 ops

`POST /api/orders`, `POST /bulk` (partial-failure), `GET` (paged + customer/status),
`GET /search|failed|pending`, `GET /{id}`, `GET /number/{n}`, `PUT /{id}`,
`GET /{id}/status|history|timeline`,
`POST /{id}/validate|submit|cancel|retry|reprocess|rollback|modify`,
`POST /{id}/payments`, `GET /{id}/payments`, `POST /{id}/payment/validate`,
`POST /{id}/promotion/apply`, `POST /{id}/fulfill[?failAt=]` (Phase 10 saga trace).
Promotions: `POST+GET /api/promotions`, `GET /{id}`.
System: ping/info.

## Inventory Service (8083) — 27 ops

`POST+GET /api/inventory/resources`, `GET /search|available`,
`GET /resources/{id}`, `GET /resources/number/{n}`, `PUT /resources/{id}`,
`DELETE /resources/{id}`, `GET /resources/{id}/history`,
`POST /quarantine|release-quarantine|reconcile`,
`GET /sim|esim|phone-numbers|devices|fiber-ports`.
Reservations: `POST /reserve|allocate|release`, `GET /reservations`,
`GET /reservations/{id}`, `GET /reservations/order/{o}`,
`POST /reservations/{id}/confirm|cancel`. System: ping/info.

## Provisioning Service (8084) — 25 ops

`POST /api/provisioning`, `POST /mobile|esim|broadband|roaming`,
`GET` (paged + type/status), `GET /failed|pending`, `GET /{id}`,
`GET /{id}/status|history`, `GET /order/{o}`,
`POST /{id}/validate|start|activate|deactivate|retry|reprocess|cancel|rollback`.
Profiles: `POST+GET /api/service-profiles`, `GET /{id}`. System: ping/info.

## Notification Service (8085) — 22 ops

`POST /api/notifications`, `POST /notify|render-preview`,
`GET` (paged + status/channel), `GET /failed|pending`, `GET /{id}`,
`GET /order/{o}`, `GET /customer/{c}`, `GET /{id}/attempts`,
`POST /{id}/send|retry|cancel`, `DELETE /{id}`.
Templates: `POST+GET /api/notification-templates`, `GET /{id}`,
`GET /code/{code}`, `POST /{id}/deactivate|activate`. System: ping/info.

## Error contract (all services)

```json
{
  "timestamp": "2026-10-08T10:15:30.123Z",
  "status": 400,
  "error": "VALIDATION_ERROR | BAD_REQUEST | INVALID_REQUEST | NOT_FOUND | CONFLICT | INTERNAL_ERROR",
  "message": "...",
  "path": "/api/..."
}
```

400 = bean validation; INVALID_REQUEST = malformed JSON/enum/date/empty body;
404 = unknown ID; 409 = unique-constraint race. No filler endpoints were added
to inflate the count — every operation maps to a Phase 2–7 business capability.
