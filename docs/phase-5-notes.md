# Phase 5 Notes — Provisioning Service domain (completed 2026-10-08)

Built on provisioning-service:8084 — `ProvisioningRequest` (6 service types × 6
statuses + attempts/lastError), `ProvisioningHistory`, `ServiceProfile`.
Deterministic simulation: MOBILE/ESIM/ROAMING need MSISDN, BROADBAND/FIBER need
resource number, DEVICE needs either — else FAILED with MISSING_* error
(foundation for Phase 13 incident scenarios). 25 ops / 23 paths.
`mvn test`: 10/10 pass. Seed: 3 profiles + 1 COMPLETED demo request.
Bug fixed: convenience endpoints declared `consumes=JSON` with no body → 415;
removed `consumes` from /mobile,/esim,/broadband,/roaming.
