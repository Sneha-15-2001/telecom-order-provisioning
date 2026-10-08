# Phase 12 Notes — Production-like logging (completed 2026-10-08)

## Where logs live now (answers "how are local logs stored")

Before: console only (captured ad-hoc to /tmp/*.log via nohup).
After: every service writes `logs/<service>/app.log` via a Logback
`SizeAndTimeBasedRollingPolicy` (10MB files, 30 daily gzipped archives, 1GB cap)
plus the console. Same `service/correlationId/event/status` pattern in both.

- Config: `<service>/src/main/resources/logback-spring.xml` (LOG_DIR overridable
  via `LOG_PATH` env; default `logs/<service>` relative to the process working dir).
- `logs/` is git-ignored. `scripts/search-logs.sh <correlationId> [orderId]`
  traces one request across all five files; `scripts/tail-all.sh` tails everything.
- `sample-logs/fulfill-order-21-TRACE.log`: real 25-line cross-service trace
  (correlation DEMO-TRACE-001) the Phase 14 investigator will consume.

## Fixes during the phase

1. Netty macOS DNS warning inherited the request correlation ID and polluted
   correlated traces → added `netty-resolver-dns-native-macos` (osx-aarch_64) to
   order-service; warning gone (verified: 0 occurrences in new traffic).
2. Relative `logs/` resolves against the process working directory — one restart
   from the wrong cwd wrote to ~/logs. Lesson documented: always start services
   from the repo root, or export an absolute LOG_PATH. Verified correct placement
   after restart from repo root.

Verified: 5/5 services UP writing rolling files; full fulfill saga traced across
all five files by one correlation ID (see sample-logs); order-service 29/29 tests.
