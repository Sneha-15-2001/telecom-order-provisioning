# Phase 13 Notes — Production incident scenarios (completed 2026-10-08)

7 reproducible business-level incidents in `incident-scenarios/`, each with a
`setup.sh` (builds the broken state, prints IDs + verify commands) and an
`INC-xxxxx.md` Jira-style card (the Phase 14 investigator input format):

| Incident | State (verified live) |
|---|---|
| INC-10101 stuck-payment | order 27 PAYMENT_PENDING, payment PENDING, no PAYMENT_VALIDATED event |
| INC-10102 esim-failure | request 14 FAILED, MISSING_MSISDN |
| INC-10103 leaked-hold | reservation 7 ACTIVE (TTL past), resource 5 RESERVED→order 9999 |
| INC-10104 notification-failed | notification 9 FAILED, provider 550 recorded in attempts |
| INC-10105 duplicate-notify | 2× ORDER_COMPLETED rows for order 2 |
| INC-10106 suspended-customer | order 30 FAILED: CUSTOMER_INVALID SUSPENDED |
| INC-10107 stuck-retry | order 29 RETRYING with no follow-up validation |

Lesson: scenario-06 first ran GREEN because interactive UI testing had flipped
customer 3 to ACTIVE — setups must ESTABLISH preconditions (setup.sh now PATCHes
the suspension first), never assume seed state. Canonical INC-10106 instance is
order 30 (order 28 is leftover from the drifted run).

## Added later (same phase): INC-10108/10109/10110

- INC-10108 N+1: order list fires ~15 item selects per 15-row page (proven via
  actuator `loggers` SQL timing; this also added `loggers` to order-service's
  actuator exposure). Fix direction recorded, deliberately not implemented.
- INC-10109 promo-vanished: FESTIVE10 applied (payable 269.10) → modify →
  promo null, payable 598.00. By-design clearing with no evidence trail.
- INC-10110 expired-hold: past-TTL confirm 400s forever; the throw rolls back
  the EXPIRED marking too, so the hold stays ACTIVE and the SIM stays RESERVED —
  a permanent leak, verified live (reservation 8 / RES-SIM0001).
