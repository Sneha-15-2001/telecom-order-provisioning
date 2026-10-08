# Phase 3 Notes — Order Service domain (completed 2026-10-08)

## What was built (order-service only, port 8082)

- Entities: `CustomerOrder` (table `telecom_order`, ORDER is reserved),
  `OrderItem`, `OrderHistory` (immutable events), `Payment` (one row per attempt),
  `Promotion` (catalog) + 7 enums covering the full lifecycle:
  CREATED→VALIDATING→VALIDATED→PAYMENT_PENDING→PAYMENT_COMPLETED→…→COMPLETED,
  plus FAILED/CANCELLED/ROLLING_BACK/RETRYING.
- No cross-service FKs: customer identity stored as plain IDs; REST calls in Phase 7.
- Services: `OrderService` (CRUD, transitions, bulk with per-item Bean Validation
  via programmatic `Validator`, modify, promotions), `PaymentService`
  (record + gateway-simulated validate), `PromotionService` (catalog).
- Controllers: `OrderController` (23 ops) + `PromotionController` (3 ops) + system (2).
- Error handler hardened from the start: 400 INVALID_REQUEST (malformed JSON/enum/date),
  409 CONFLICT, plus the standard contract.
- Real SQL: 5 tables + CHECKs, 10 indexes, idempotent seed (3 promos incl. expired one,
  1 VALIDATED demo order). Validated by drop→create→seed.
- Business rules verified live: submit only from VALIDATED; payments only on
  PAYMENT_PENDING; gateway approves iff paid ≥ payable; promo double-apply rejected;
  expired/inactive promos rejected; modify clears promo + resets to CREATED;
  cancel blocked from COMPLETED/CANCELLED; retry only from FAILED; reprocess
  restarts at CREATED; rollback only from in-flight states.

## Verification

| Check | Result |
|---|---|
| `mvn test` order-service | 11/11 pass, BUILD SUCCESS (6 service-unit incl. bulk partial-failure, 1 repo, 3 controller, 1 context) |
| Live sweep | create→promo→validate→submit→pay→PAYMENT_COMPLETED; short-pay→FAILED→retry→RETRYING; modify/cancel/reprocess flows; bulk partial [success+error]; failed/pending lists; promo catalog; PUT; 404/400s |
| Swagger | 24 paths / 28 ops at :8082/swagger-ui |
| Bug found & fixed | class-level `@Valid` on bulk wrapper aborted whole batch → per-item programmatic validation |

## Endpoint inventory (order-service, 8082)

POST/GET(paged)/search/failed/pending/GET{id}/GET number/{n}/PUT,
GET status/history/timeline, POST validate/submit/cancel/retry/reprocess/rollback/modify,
POST+GET payments, POST payment/validate, POST promotion/apply, POST/GET/GET{id} promotions,
bulk. Total: 28 operations, 24 paths.
