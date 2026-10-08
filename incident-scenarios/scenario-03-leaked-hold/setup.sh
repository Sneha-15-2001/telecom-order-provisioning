#!/bin/bash
# Scenario 03 (INC-10103): leaked reservation — resource reserved, never confirmed/released.
# The MSISDN sits RESERVED forever, blocking reuse.
set -uo pipefail
B3=http://localhost:8083
CID="INC-10103-$(date +%H%M%S)"
RID=$(curl -s -m 5 "$B3/api/inventory/available?type=MSISDN&limit=1" -H "X-Correlation-ID: $CID" \
  | python3 -c "import json,sys; print(json.load(sys.stdin)[0]['id'])")
S=$(curl -s -m 5 -X POST $B3/api/inventory/reserve -H 'Content-Type: application/json' -H "X-Correlation-ID: $CID" \
  -d "{\"resourceId\":$RID,\"orderId\":9999,\"customerId\":1,\"ttlMinutes\":1}")
SID=$(echo "$S" | python3 -c "import json,sys; print(json.load(sys.stdin)['id'])")
SNUM=$(echo "$S" | python3 -c "import json,sys; print(json.load(sys.stdin)['reservationNumber'])")
echo "INC-10103 reservationId=$SID reservationNumber=$SNUM resourceId=$RID correlationId=$CID"
echo "verify: curl $B3/api/inventory/resources/$RID  (expect RESERVED, orderId=9999)"
echo "verify: curl $B3/api/inventory/reservations/$SID  (ACTIVE, expiring unnoticed)"
