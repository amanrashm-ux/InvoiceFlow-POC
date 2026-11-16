# Local POC runbook

## First run

1. `cp .env.example .env` and edit local passwords.
2. `docker compose --env-file .env -f infra/docker-compose.yml up --build -d`.
3. `docker compose --env-file .env -f infra/docker-compose.yml ps` — wait for RabbitMQ health and Java service startup. Each Java service initializes its own H2 file database on first start.
4. `bash scripts/seed-demo.sh`.
5. Open `http://localhost:3000`, refresh after ~10-20 seconds for asynchronous events.

## Test the APIs

- `GET /api/parties`
- `GET /api/invoices`
- `POST /api/invoices` with the JSON example in the root README
- `POST /api/invoices/{invoiceId}/accept`
- `GET /api/dashboard/summary`

## Observe RabbitMQ

Open `http://localhost:15672` with `.env` RabbitMQ credentials. Check exchange `invoiceflow.domain`, queue depths, unacked messages, and `invoiceflow.dead-letter`.

## Seed expectations

The script creates four invoices and accepts three. Two accepted invoices are within the INR 50 lakh demo threshold and should flow through financing/payment/reconciliation. The accepted INR 51 lakh record should be marked risk-ineligible; the INR 87,500 invoice remains submitted and awaits buyer acceptance. Eligible invoices flow automatically through financing, simulated payment and reconciliation.

## Known POC limitations

- Services may take additional seconds to become ready after infrastructure health checks.
- The first seed run creates unique invoice numbers; run again for more test records (it does not truncate prior data).
- If you need a clean run, `docker compose down -v` removes all local POC data and then start again.
- Spark consumer and Snowflake batch loader are separately executed; core business services and the React portal run in Compose.
- Snowflake load requires a real Snowflake account and compatible connector package configuration.
