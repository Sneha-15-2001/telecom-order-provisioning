# Phase 16 Notes — Temporary data-fix recommender (completed 2026-10-09)

`POST /api/datafix`: rule-built proposals from investigation output —
free leaked holds (CANCELLED + AVAILABLE with validation SELECT + rollback),
complete skipped payment validation via API op, retry failed notifications,
cancel duplicate extras, review-only for truthful states (suspended customer).
Every proposal: kind (sql/api/review), why, SQL, validation, rollback,
requires_approval=True. No write path exists anywhere — proposal text only.
LLM-drafted SQL embedded when LLM mode returns it. UI: "Propose fixes" button
renders data-fix cards with SQL blocks + validation/rollback notes.
Verified: leaked hold → Free RES-MSISDN02 SQL; suspended → review-only.
