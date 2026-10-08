# Phase 4 Notes — Inventory Service domain (completed 2026-10-08)

Built on inventory-service:8083 — `InventoryResource` (8 types × 6 statuses),
`Reservation` (ACTIVE/CONFIRMED/CANCELLED/EXPIRED + TTL expiry), `InventoryHistory`.
Services: resource catalog (register/search/available/quarantine/reconcile/history,
cleanup delete) + reserve→confirm/allocate→release/cancel.
27 ops / 24 paths. `mvn test`: 9/9 pass.
Bugs fixed: Mockito chained @InjectMocks (manual wiring); DELETE blocked by audit FKs
→ documented cleanup delete removing child rows (production: reconcile to DECOMMISSIONED).
Seed: 10 resources incl. 1 QUARANTINED router. Docs: full sweep in shell history.
