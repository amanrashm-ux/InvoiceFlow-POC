# Scala + Spark + Snowflake

## Runtime pairing

This POC uses Scala 2.12 / Spark 3.4.x. This scaffold pins Snowflake Spark Connector `3.2.0` (`spark-snowflake_2.12`) and Snowflake JDBC `4.0.2` alongside Scala 2.12.18 / Spark 3.4.1. Snowflake documents the 3.x connector line for Spark 3.2–4.1 and the artifact naming for Scala 2.12 / 2.13. Re-check connector release notes before upgrading Spark or Scala: https://docs.snowflake.com/en/user-guide/spark-connector-install and https://docs.snowflake.com/en/release-notes/clients-drivers/spark-connector-2026.

## Processes

- `RawEventConsumer`: durable RabbitMQ queue `analytics.domain-events` → newline-delimited JSON raw landing file. The consumer acknowledges only after appending the event.
- `SparkToSnowflake`: reads the raw JSONL file, deduplicates by event ID and writes structured event records to `INVOICEFLOW.BRONZE.RAW_DOMAIN_EVENTS`.

Build with sbt. Example:

```bash
sbt package
```

Provide `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RAW_EVENT_PATH`, `SNOWFLAKE_URL`, `SNOWFLAKE_USER`, `SNOWFLAKE_PASSWORD`, `SNOWFLAKE_WAREHOUSE`, and optionally `SNOWFLAKE_DATABASE` / `SNOWFLAKE_SCHEMA`.

Snowflake credentials are intentionally not included. Prefer key-pair auth / workload identity and a secret manager in production. For production landing, use durable object storage (S3 / ADLS / GCS), not a container-local file. Add schema validation, corrupt-record handling, streaming checkpoints and replay tooling before production.
