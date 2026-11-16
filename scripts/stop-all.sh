#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if ! command -v docker >/dev/null 2>&1 && [[ -x "$HOME/.docker/bin/docker" ]]; then
  export PATH="$HOME/.docker/bin:$PATH"
fi
[[ -f "$ROOT/.env" ]] || { echo "No .env found; nothing to stop."; exit 0; }
docker compose --env-file "$ROOT/.env" -f "$ROOT/infra/docker-compose.yml" down
