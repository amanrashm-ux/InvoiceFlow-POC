# InvoiceFlow — End-to-End POC

A runnable, event-driven invoice discounting POC. It includes independently deployable Spring Boot services, H2 file database-per-service, RabbitMQ, a role-based React SPA, a Scala/Spark analytics pipeline, Snowflake DDL, and seeded demo data.

## Demo journey

1. Supplier submits an invoice.
2. Buyer accepts it.
3. Risk Service evaluates the accepted invoice.
4. Financing Service creates an approval when eligible.
5. Payment Service creates a **simulated** disbursement.
6. Reconciliation Service matches the payment to the invoice.
7. All domain events are copied to the analytics queue, landed as raw JSON, transformed by Spark, and loaded to Snowflake.
8. The React portal displays operational statuses and dashboard metrics.

## Stack

- React + TypeScript + Vite
- Java 21, Spring Boot 3.3.5, Spring Cloud Gateway 2023.0.3
- H2 2.x — separate embedded file database for each service (no PostgreSQL container)
- RabbitMQ 3.13 — topic exchange, durable queues, dead-letter exchange
- Scala 2.12.18 + Spark 3.4.1
- Snowflake Spark Connector (configured as a version-matched runtime dependency)
- Docker Compose; Kubernetes migration notes included

## Start locally — one command

Prerequisite: Docker Desktop / Docker Engine with Compose v2. Java 21 is used by the services; Maven and Node are supplied in the build containers, so you do not need to install Homebrew, Maven, or Node on the Mac just to launch the POC.

After Docker Desktop is installed and running, start the entire local core platform and seed the demo:

```bash
./scripts/start-all.sh
```

This builds and starts RabbitMQ, the eight Spring Boot services, API Gateway and React portal; waits for the gateway; seeds synthetic demo invoices; runs smoke checks; and opens the portal. For no-admin macOS setup, see `docs/MAC-SETUP.md`.

Open:

- Portal: http://localhost:3000
- API Gateway: http://localhost:8080
- RabbitMQ management: http://localhost:15672
- H2 databases: embedded in each service; files persist in dedicated Docker volumes

The seed script uses the seeded three suppliers and two buyers, and creates sample invoices through the public service APIs. It accepts selected invoices to trigger the complete asynchronous chain.

## Useful commands

```bash
# Stop the stack without deleting persistent demo data
./scripts/stop-all.sh
```


```bash
# Follow the event-driven services
docker compose --env-file .env -f infra/docker-compose.yml logs -f invoice-service risk-service financing-service payment-service reconciliation-service

# Inspect all containers
docker compose --env-file .env -f infra/docker-compose.yml ps

# Stop services; keep data volumes
docker compose --env-file .env -f infra/docker-compose.yml down

# Reset all local POC data (DELETES volumes)
docker compose --env-file .env -f infra/docker-compose.yml down -v
```

## API examples

```bash
curl http://localhost:8080/api/invoices
curl -X POST http://localhost:8080/api/invoices \
  -H 'Content-Type: application/json' \
  -d '{"supplierId":"SUP-001","buyerId":"BUY-001","invoiceNumber":"INV-DEMO-9001","currency":"INR","amount":125000,"issueDate":"2026-11-01","dueDate":"2026-12-01"}'
```

The invoice response includes `invoiceId`. Buyer acceptance:

```bash
curl -X POST http://localhost:8080/api/invoices/INVOICE_ID/accept
```

Dashboard API: `GET http://localhost:8080/api/dashboard/summary`.

## Snowflake setup

Snowflake is external and requires your account credentials. Execute `database/snowflake/00_bootstrap.sql`, `01_bronze.sql`, and `02_silver_gold.sql` in order. Configure the Spark job using `data-platform/spark-pipeline/.env.example`. The Spark Connector artifact must match the selected Spark and Scala binary versions; see the pipeline README. No Snowflake account or credentials are bundled.

## Important POC boundaries

- Payment is simulated. No banking/payment rail is contacted.
- Demo risk rules are deterministic examples, not a credit decisioning model.
- Identity endpoint and UI role switch are demo scaffolding, not production authentication. Do not expose this POC publicly.
- RabbitMQ uses at-least-once delivery. Consumers are designed to be idempotent by event ID where implemented.
- Invoice Service includes a transactional outbox. Other service event publications are simplified for POC speed and should adopt the same outbox/inbox pattern before production.
- For this H2 POC, Invoice Service reads use the same embedded database as writes. H2 embedded mode does not supply physical read replicas; see `docs/READ-REPLICA.md` for the production migration path.
- Snowflake is analytical storage, not an OLTP system of record.

See `docs/POC-RUNBOOK.md`, `docs/EVENT-CATALOGUE.md` and `docs/ARCHITECTURE.md`.
