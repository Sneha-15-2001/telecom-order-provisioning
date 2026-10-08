# Phase 11 Notes — Testing/Swagger/validation/docs polish (completed 2026-10-08)

New tests (94 total, all green): SubscriptionServiceTest (7), CorporateAccountServiceTest (4),
PaymentServiceTest (5), PromotionServiceTest (4), InventoryAdminTest (reconcile/delete/quarantine
rules), ProvisioningLifecycleTest (retry/rollback/deactivate/reprocess/validate, 6),
NotificationRulesTest (cancel/delete/template rules, 6).
Suites: customer 21, order 29, inventory 12, provisioning 16, notification 16.

Swagger: 201/400/404/204 @ApiResponse codes added to all primary mutating endpoints
(create/bulk/fulfill/payment-validate/reserve/reconcile/activate/send/delete…).
Validation audit: every request DTO re-checked; real fix — @Pattern on
ProvisioningController query params was silently ignored without @Validated;
added @Validated so bad MSISDN input now 400s (verified live).
Docs: new docs/testing.md; README state refresh; api-overview → 128 ops (fulfill added).
