#!/usr/bin/env bash
set -euo pipefail
BASE="${API_BASE:-http://localhost:8080}"
echo "Checking gateway-routed endpoints..."
curl -fsS "$BASE/api/parties" >/dev/null
curl -fsS "$BASE/api/invoices" >/dev/null
curl -fsS "$BASE/api/dashboard/summary" >/dev/null
echo "Core HTTP endpoints responded. For full business-flow verification, run scripts/seed-demo.sh and inspect dashboard counts."
