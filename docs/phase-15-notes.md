# Phase 15 Notes — Structured RCA documents (completed 2026-10-08)

`POST /api/rca` = investigate() + `rca.py` document builder. Sections: incident,
summary, impact (status→business-impact mapping), affected (order/customer/service),
timeline + window, evidence (correlations, line count, tables read), root cause,
contributing factors (rule-derived: missing callback, no sweeper, no idempotency,
unvalidated identifiers), confidence, fix_type, recommendation (data-first vs
code-first from fix_hint). LLM mode embeds narrative (evidence chain + next steps).

UI: "Generate RCA document" button under results + .md download. Verified:
INC-10101 rules RCA (6-event timeline, PAYMENT gap contributing factor) and
INC-10106 markdown render; LLM-mode RCA embeds narrative (confidence from model).
Fixed along the way: an edit that ate the `investigate()` declaration (restored).
