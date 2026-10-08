# Phase 2 Notes — Customer Service domain (completed 2026-10-08)

## What was built (customer-service only, port 8081)

- Entities: `Customer`, `CustomerAddress`, `Subscription`, `CorporateAccount` +
  enums `CustomerStatus` (ACTIVE/INACTIVE/SUSPENDED/BLOCKED), `CustomerType`,
  `AddressType`, `SubscriptionStatus`.
- Repositories with finder + JPQL search methods.
- 14 request/response DTOs with Jakarta validation; manual `CustomerMapper`.
- Services: `CustomerService` (CRUD, status transitions, validate/eligibility
  contract for Phase 7, history, addresses, corporate linking),
  `SubscriptionService`, `CorporateAccountService`. Structured logs per operation.
- Controllers: `CustomerController` (19 ops) + `CorporateAccountController` (4 ops).
- Real SQL: `database/customer/01_create_tables.sql` (4 tables, PK/FK/UNIQUE/CHECK),
  `02_indexes.sql` (8 indexes), `03_sample_data.sql` (idempotent demo data).
  Scripts validated by drop → create → index → seed on `telecom_customer`.
- Business rules: unique email/phone enforced; suspend only from ACTIVE;
  reactivate only from SUSPENDED/INACTIVE; MSISDN unique across subscriptions;
  primary-address demotion; cancel stamps endDate; delete cascades addresses+subscriptions.

## Verification

| Check | Result |
|---|---|
| `mvn test` customer-service | 10/10 pass, BUILD SUCCESS (unit 5, repo 1, controller 3, context 1) |
| Other 4 services untouched | rebuilt/started earlier, still on Phase 1 foundation |
| Live endpoint sweep (curl) | 27 checks OK: CRUD, search, status flows, validate, eligibility, history, addresses, subscriptions lifecycle, corporate CRUD + link, DELETE 204, 404, validation 400 |
| Swagger `/v3/api-docs` | 19 paths / 25 operations |
| Sample data | 3 customers, 1 corporate, 1 address, 1 subscription |

## Endpoint inventory (customer-service, 8081)

Customers: POST/GET(paged+status)/search/GET{id}/GET number/{n}/PUT/DELETE,
PATCH status, POST suspend/reactivate/validate, GET eligibility/history,
GET+POST addresses, GET+POST+PUT+POST-cancel subscriptions.
Corporate: POST/GET/GET{id}, POST {id}/customers/{customerId}.
System: GET ping/info (unchanged). Total: 25 operations, 19 paths.
