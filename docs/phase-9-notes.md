# Phase 9 Notes — Angular frontend (completed 2026-10-08)

NexaTel-branded ops console (original brand, Airtel/Jio-inspired navy + red theme):
Angular 22 standalone, Router, HttpClient + X-Correlation-ID interceptor,
Reactive/Template-driven forms, signals.

Screens (10 routes): `/` dashboard (live KPIs via forkJoin, failed-orders queue,
service health via actuator), `/customers`, `/customers/new`, `/customers/:id`
(profile, suspend/reactivate, validate-for-order, eligibility, subscriptions, history),
`/orders`, `/orders/new`, `/orders/:id` (lifecycle buttons, pay+validate, promo apply,
items, timeline), `/inventory` (filters, reserve→confirm→release), `/provisioning`
(quick mobile activation, start/activate/retry/rollback/deactivate),
`/notifications` (queue via templates, send/retry).

Verified: `npm run build` success, `npx ng test` 2/2 pass, dev server :4200 serves
new bundle (NexaTel ×4 in main.js), all 9 routes HTTP 200, backend data loads
(the dashboard consumes the same endpoints proven by curl in Phases 2–7; browser
calls use absolute localhost URLs covered by backend CORS for :4200).
Fixed along the way: dashboard forEach paren typo, stale app.spec, dev-server
serving a mid-write failed bundle (restarted clean).
Incident/RCA/data-fix/code-fix screens arrive with Phases 14–17.
