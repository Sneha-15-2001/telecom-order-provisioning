# Phase 6 Notes — Notification Service domain (completed 2026-10-08)

Built on notification-service:8085 — `Notification` (5 statuses, attempts,
providerMessageId), `NotificationTemplate` ({{placeholders}}), `NotificationAttempt`.
Services: template-rendered queueing, one-call `notifyEvent` (event=template code),
simulated delivery (recipients containing "fail" deterministically fail),
retry/cancel/delete, render preview, template catalog + activate/deactivate.
22 ops / 19 paths. `mvn test`: 10/10 pass.
Bug fixed during build: `deliver()` return-type mismatch (compile error).
Seed: 4 templates. Full live sweep OK (render, send, fail→retry, cancel→delete).
