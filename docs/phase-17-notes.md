# Phase 17 Notes — Permanent code-fix recommender (completed 2026-10-09)

`POST /api/codefix`: defect-family → change proposal (service, area, problem,
before/after, tests, risks, deploy notes). Families: expiry-rollback-eats-fix
(REQUIRES_NEW/sweeper), notify idempotency key, N+1 entity-graph, skipped-step
guard + reconciler, promo-removal audit event, sales eligibility pre-check,
provisioning identifier required at create. Matching uses suspect + incident
text + journey events + every last_error in evidence (widened after the demo
run missed MISSING_RESOURCE sitting in a provisioning row, not the signal).
LLM-drafted fix embedded when present. UI renders before/after + tests.
Verified: N+1 → entity-graph proposal; suspended → pre-check; broadband demo →
require+auto-attach identifiers.
Fixed along the way: an edit that commented out the suspended-customer rule
(caught by re-running its ticket — re-verify after every edit).
