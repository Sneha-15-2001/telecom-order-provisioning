#!/bin/bash
# Scenario 10 (INC-10110): expired hold leaks — confirm after TTL marks the
# reservation EXPIRED but the resource stays RESERVED (compensation gap).
set -uo pipefail
B3=http://localhost:8083
CID="INC-10110-$(date +%H%M%S)"
RID=$(curl -s -m 5 "$B3/api/inventory/available?type=SIM&limit=1" -H "X-Correlation-ID: $CID" \
  | python3 -c "import json,sys; print(json.load(sys.stdin)[0]['id'])")
S=$(curl -s -m 5 -X POST $B3/api/inventory/reserve -H 'Content-Type: application/json' -H "X-Correlation-ID: $CID" \
  -d "{\"resourceId\":$RID,\"orderId\":8888,\"customerId\":1,\"ttlMinutes\":1}")
SID=$(echo "$S" | python3 -c "import json,sys; print(json.load(sys.stdin)['id'])")
echo "reserved $SID on resource $RID — waiting 70s for TTL expiry..."
sleep 70
echo "--- confirm after expiry: ---"
curl -s -m 5 -X POST $B3/api/inventory/reservations/$SID/confirm -H "X-Correlation-ID: $CID"
echo
echo "INC-10110 reservationId=$SID resourceId=$RID correlationId=$CID"
echo "verify: curl $B3/api/inventory/reservations/$SID  (still ACTIVE — rollback ate the EXPIRED marking)"
echo "verify: curl $B3/api/inventory/resources/$RID  (STILL RESERVED = the leak; confirm 400s forever)"
