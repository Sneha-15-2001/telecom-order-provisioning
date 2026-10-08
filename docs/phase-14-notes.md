# Phase 14 Notes — AI Production Incident Investigator (completed 2026-10-08)

New standalone app: Python FastAPI backend (:8090) + React/Vite frontend (:5173)
under `ai-investigator/`. Strictly READ-ONLY (log grep + SELECTs; safety note in
every response). Human approval stays mandatory for any fix (16–17).

Pipeline: regex extraction → order-number→id resolution via SELECT → targeted
log search → correlation expansion → journey → focus-service suspect + hypothesis
→ DB evidence bundle. Verified live against real incidents:
- INC-10101 (stuck payment): suspect order-service, "PAYMENT_RECORDED without
  PAYMENT_VALIDATED", medium; DB shows PAYMENT_PENDING + PENDING payment
- INC-10102 (eSIM): suspect provisioning-service PROVISIONING_FAILED, high;
  DB shows FAILED + MISSING_MSISDN
- INC-10106 (suspended): suspect order-service ORDER_FAILED, high; journey shows
  CUSTOMER_STATUS_CHANGED + CUSTOMER_VALIDATED → ORDER_FAILED

Bugs fixed during build: LOG_ROOT off-by-one parent (ai-investigator/logs vs
repo logs); bare "27" needle matched timestamps (qualified forms only now);
orderId=2 substring-matched orderId=21/27 (digit-boundary regex); suspect picked
incidental notification failure over ticket-focus service (focus-service rule).
React UI: ticket box + 4 sample incidents, hypothesis card, entities, DB JSON,
journey timeline, full log lines. `npm run build` + served :5173, backend
uvicorn :8090, `/docs` live.
