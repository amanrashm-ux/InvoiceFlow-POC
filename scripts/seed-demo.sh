#!/usr/bin/env bash
set -euo pipefail
BASE="${API_BASE:-http://localhost:8080}"
wait_for(){ local url="$1"; for i in $(seq 1 60); do if curl -fsS "$url" >/dev/null 2>&1; then return 0; fi; sleep 2; done; echo "Timed out waiting for $url" >&2; exit 1; }
echo "Waiting for InvoiceFlow services..."
wait_for "$BASE/api/invoices"
wait_for "$BASE/api/parties"
echo "Creating synthetic demo invoices..."
create_invoice(){
  local supplier="$1" buyer="$2" number="$3" amount="$4" issue="$5" due="$6"
  curl -fsS -X POST "$BASE/api/invoices" -H 'Content-Type: application/json' \
    -d "{\"supplierId\":\"$supplier\",\"buyerId\":\"$buyer\",\"invoiceNumber\":\"$number\",\"currency\":\"INR\",\"amount\":$amount,\"issueDate\":\"$issue\",\"dueDate\":\"$due\"}"; echo
}
# Unique invoice numbers allow re-running the script after a clean DB reset. Use date stamp.
STAMP="$(date +%s)"
R1="$(create_invoice SUP-001 BUY-001 "AA-$STAMP-01" 125000 2026-11-01 2026-12-01)"
R2="$(create_invoice SUP-002 BUY-001 "DP-$STAMP-02" 240000 2026-11-03 2026-12-03)"
R3="$(create_invoice SUP-003 BUY-002 "ET-$STAMP-03" 87500 2026-11-05 2026-12-05)"
R4="$(create_invoice SUP-001 BUY-002 "AA-$STAMP-04" 5100000 2026-11-06 2026-12-06)"
ids=("$(python3 -c 'import json,sys;print(json.loads(sys.argv[1])["invoiceId"])' "$R1")" "$(python3 -c 'import json,sys;print(json.loads(sys.argv[1])["invoiceId"])' "$R2")" "$(python3 -c 'import json,sys;print(json.loads(sys.argv[1])["invoiceId"])' "$R3")" "$(python3 -c 'import json,sys;print(json.loads(sys.argv[1])["invoiceId"])' "$R4")")
for i in 0 1 3; do echo "Accepting ${ids[$i]}"; curl -fsS -X POST "$BASE/api/invoices/${ids[$i]}/accept"; echo; done
echo "Seeded 4 synthetic invoices; 3 accepted. Risk limit will reject the INR 51 lakh sample."
echo "Wait 10-20 seconds, then refresh the portal / dashboard."
