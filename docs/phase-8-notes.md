# Phase 8 Notes — API audit (completed 2026-10-08)

No new code needed: live `/v3/api-docs` audit shows 127 meaningful operations
(109 paths) across the five services — target of ~100 already met without filler:
customer 25, order 28, inventory 27, provisioning 25, notification 22.
Every operation traces to a Phase 2–7 business capability (see docs/api-overview.md
for the full inventory). Cross-cutting standards verified on all services:
DTO-only contracts, Bean Validation, standard error codes
(VALIDATION_ERROR/BAD_REQUEST/INVALID_REQUEST/NOT_FOUND/CONFLICT),
`@ParameterObject` paging, Swagger examples, correlation-ID logging.
