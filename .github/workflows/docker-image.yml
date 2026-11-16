#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT/infra/docker-compose.yml"
ENV_FILE="$ROOT/.env"
API_BASE="${API_BASE:-http://localhost:8080}"

info() { printf '\033[1;34m[InvoiceFlow]\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[InvoiceFlow]\033[0m %s\n' "$*" >&2; }
fail() { printf '\033[1;31m[InvoiceFlow ERROR]\033[0m %s\n' "$*" >&2; exit 1; }

cd "$ROOT"

# Docker Desktop installs its CLI in the per-user directory by default on macOS.
if ! command -v docker >/dev/null 2>&1 && [[ -x "$HOME/.docker/bin/docker" ]]; then
  export PATH="$HOME/.docker/bin:$PATH"
fi

if ! command -v docker >/dev/null 2>&1; then
  cat >&2 <<'MSG'
Docker CLI was not found.
Install Docker Desktop for Mac first (see docs/MAC-SETUP.md). No Homebrew,
Maven, or sudo installation is required for this POC's normal Docker workflow.
After Docker Desktop is installed, reopen Terminal and run this script again.
MSG
  exit 2
fi

if ! docker compose version >/dev/null 2>&1; then
  fail "Docker Compose v2 is not available. Update/start Docker Desktop and retry."
fi

# Docker Desktop can use a per-user socket when the system-wide socket is disabled.
if [[ -z "${DOCKER_HOST:-}" && -S "$HOME/.docker/run/docker.sock" ]]; then
  export DOCKER_HOST="unix://$HOME/.docker/run/docker.sock"
fi

if ! docker info >/dev/null 2>&1; then
  if [[ -d "/Applications/Docker.app" ]]; then
    info "Starting Docker Desktop..."
    open -a "/Applications/Docker.app" >/dev/null 2>&1 || true
  elif [[ -d "$HOME/Applications/Docker.app" ]]; then
    info "Starting Docker Desktop from ~/Applications..."
    open "$HOME/Applications/Docker.app" >/dev/null 2>&1 || true
  else
    fail "Docker Engine is not running. Open Docker Desktop, wait until it says Engine running, then retry."
  fi

  for attempt in $(seq 1 90); do
    if docker info >/dev/null 2>&1; then break; fi
    sleep 2
  done
  docker info >/dev/null 2>&1 || fail "Docker Engine did not become ready. Open Docker Desktop and inspect its startup message."
fi

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ROOT/.env.example" "$ENV_FILE"
  info "Created .env from .env.example. Local defaults are for this demo machine only."
fi

info "Building all InvoiceFlow containers (limited parallelism for laptop memory)..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build

info "Starting RabbitMQ, eight Spring Boot services, API Gateway and React portal..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d

info "Waiting for API Gateway and core endpoints..."
READY=0
for attempt in $(seq 1 120); do
  if curl -fsS "$API_BASE/api/parties" >/dev/null 2>&1 \
    && curl -fsS "$API_BASE/api/invoices" >/dev/null 2>&1; then
    READY=1
    break
  fi
  sleep 3
done

if [[ "$READY" -ne 1 ]]; then
  warn "The API did not become ready. Recent container logs follow:"
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps || true
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=100 || true
  fail "Startup did not pass readiness checks. Resolve the first service error in the logs and rerun ./scripts/start-all.sh."
fi

info "Creating synthetic suppliers' invoices and accepting selected invoices..."
bash "$ROOT/scripts/seed-demo.sh"

info "Running HTTP smoke checks..."
bash "$ROOT/scripts/smoke-test.sh"

printf '\n\033[1;32mInvoiceFlow core POC is running.\033[0m\n'
printf 'Portal:                 http://localhost:3000\n'
printf 'API Gateway:            http://localhost:8080\n'
printf 'RabbitMQ Management:    http://localhost:15672\n'
printf 'RabbitMQ user:          %s\n' "$(awk -F= '/^RABBITMQ_USER=/{print $2}' "$ENV_FILE")"
printf 'RabbitMQ password:      value in %s\n' "$ENV_FILE"
printf '\n'
printf 'View service status:    docker compose --env-file .env -f infra/docker-compose.yml ps\n'
printf 'Follow logs:            docker compose --env-file .env -f infra/docker-compose.yml logs -f\n'
printf 'Stop all:               ./scripts/stop-all.sh\n\n'
warn "Scala/Spark to Snowflake is a separate analytics job and requires your Snowflake account settings; see data-platform/spark-pipeline/README.md."

if [[ "${NO_OPEN:-0}" != "1" ]] && command -v open >/dev/null 2>&1; then
  open "http://localhost:3000" >/dev/null 2>&1 || true
fi
