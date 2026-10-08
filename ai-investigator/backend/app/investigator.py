"""Investigation orchestration (Phase 14).

Pipeline: incident text -> extract identifiers -> targeted log search ->
correlate -> journey -> suspect service + hypothesis. RCA text (15), data-fix
(16) and code-fix (17) build on this output in later phases.
"""

from . import dbcheck, extractor, llmreason, logsearch

# event -> owning service is known from the log line itself; these rules turn
# a journey into a suspect + hypothesis.
FAILED_MARKERS = ("FAILED",)


def _suspect(entities: dict, journey: list) -> dict:
    # Most specific identifier in the ticket sets the focus service.
    focus = None
    if entities.get("request_number"):
        focus = "provisioning-service"
    elif entities.get("reservation_number") or entities.get("resource_number"):
        focus = "inventory-service"
    elif entities.get("notification_number"):
        focus = "notification-service"
    pool = [j for j in journey if j["service"] == focus] if focus else journey

    failed = [j for j in (pool or journey)
              if j["status"] in FAILED_MARKERS or "FAIL" in j["event"]]
    if failed:
        last = failed[-1]
        return {
            "service": last["service"],
            "signal": f"{last['event']} status={last['status']}",
            "confidence": "high",
        }
    # stuck patterns: recorded step with no follow-up
    events = [j["event"] for j in journey]
    stuck_rules = [
        ("PAYMENT_RECORDED", "PAYMENT_VALIDATED", "order-service",
         "payment recorded but never validated — validation step skipped, not failed"),
        ("RESOURCE_RESERVED", "RESOURCE_ALLOCATED", "inventory-service",
         "resource reserved but never confirmed/released — leaked hold"),
        ("STATUS_CHANGED", None, None, None),
    ]
    for started, expected, svc, why in stuck_rules:
        if started in events and expected and expected not in events:
            return {"service": svc, "signal": f"{started} without {expected}", "confidence": "medium",
                    "hypothesis": why}
    if journey:
        return {"service": journey[-1]["service"],
                "signal": f"last observed: {journey[-1]['event']}",
                "confidence": "low"}
    return {"service": None, "signal": "no correlated log lines found", "confidence": "none"}


def _hypothesis(entities: dict, suspect: dict, db: dict) -> str:
    hints = entities.get("error_hints", [])
    if hints:
        joined = ", ".join(hints[:4])
    else:
        joined = "no explicit error token in the ticket"
    orders = db.get("orders", [])
    state = f"order state: {orders[0]['status']}" if orders else "order state unknown"
    svc = suspect["service"] or "unidentified service"
    return (f"Likely fault in {svc} ({suspect['signal']}; {state}). "
            f"Ticket hints: {joined}. "
            f"Confidence {suspect['confidence']} — verify against the journey + DB below.")


def investigate(incident_text: str) -> dict:
    entities = extractor.extract(incident_text)
    entities = dbcheck.resolve_order_ids(entities)  # numbers -> ids before log search
    logs = logsearch.search(entities)
    db = dbcheck.collect(entities, logs)
    suspect = _suspect(entities, logs["journey"])
    result = {
        "incident_text": incident_text,
        "entities": entities,
        "correlations": logs["correlations"],
        "journey": logs["journey"],
        "log_lines": logs["log_lines"],
        "log_stats": {"direct_hits": logs["direct_hits"], "lines": len(logs["log_lines"])},
        "db_evidence": db,
        "suspect": suspect,
        "hypothesis": _hypothesis(entities, suspect, db),
        "safety": "READ-ONLY: searched logs + SELECTs only. No data changed, nothing deployed.",
    }
    # LLM reasoner on top of the deterministic evidence (disabled without a key).
    result["llm"] = llmreason.reason(incident_text, {
        "entities": entities,
        "journey": logs["journey"],
        "suspect": suspect,
        "rule_hypothesis": result["hypothesis"],
        "db_evidence": db,
    })
    return result
