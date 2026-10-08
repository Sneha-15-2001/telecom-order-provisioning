# Phase 10 Notes — End-to-end fulfillment saga (completed 2026-10-08)

`POST /api/orders/{id}/fulfill[?failAt=]` in order-service runs the whole chain for
PAYMENT_COMPLETED orders: auto-pick reservable item → firstAvailable+reserve →
INVENTORY_RESERVED → provision create/start/activate → ACTIVATING → confirm
reservation → COMPLETED → notify via customer phone (best effort). Item→resource/
service mapping covers SIM/eSIM/MSISDN/DEVICE/FIBER/ONT; reserved MSISDN identifier
feeds activation; ADDON-only orders skip reservation.

Compensation: reserve/activate failure → cancel reservation + roll back provisioning
(both best-effort, never masking the cause) → order FAILED with reason recorded in
history. `failAt=RESERVE|ACTIVATE|NOTIFY` demo hooks force each failure path.
Every step is appended to both the response trace and order history.

Verified: 20/20 order-service tests (happy-path + compensation unit tests, stub
tests for available/profile/rollback); live: mobile order → COMPLETED (reservation
CONFIRMED, provisioning COMPLETED, SMS queued), broadband failAt=ACTIVATE → FAILED
with reservation CANCELLED + provisioning ROLLED_BACK, device order → COMPLETED.
UI: order page has "Fulfill end-to-end" + "fail at activate" buttons with step trace.
