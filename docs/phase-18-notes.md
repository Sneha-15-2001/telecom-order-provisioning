# Phase 18 Notes — Hackathon demo (completed 2026-10-09)

`demo/run-hackathon-demo.sh`: 7-step live arc (~2 min) — order+pay broadband,
fulfill with injected activation fault, ticket text, chatbot investigate, RCA,
data-fix, code-fix proposals. Self-sufficient (mints its own fiber port —
learned from pool exhaustion breaking the first run). Verified end to end:
order FAILED with compensation trace, suspect provisioning-service, DATA hint,
no-op datafix (already compensated — honest), require+auto-attach codefix.
Same arc clickable in NexaTel Incidents page + :5173 console.
