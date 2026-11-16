# Architecture & service ownership

| Deployable | Port | Owned H2 database | Primary responsibility |
|---|---:|---|---|
| API Gateway | 8080 | none | Path routing to backend services |
| Invoice Service | 8081 | invoice_db | Invoice lifecycle, transactional outbox |
| Risk Service | 8082 | risk_db | Rule-based eligibility on invoice acceptance |
| Financing Service | 8083 | financing_db | POC financing application and approval |
| Payment Service | 8084 | payment_db | Simulated disbursement |
| Reconciliation Service | 8085 | reconciliation_db | Event-based match record |
| Party Service | 8086 | party_db | Supplier/buyer master data |
| Identity Service | 8087 | identity_db | Demo user identities (not production auth) |
| Read Model Service | 8088 | read_model_db | Event log and dashboard projection |

Each Java service embeds its own persistent H2 file database. Databases are isolated by service; services communicate through versioned events and APIs and do not share tables. Docker Compose mounts an individual named volume for each service database.

The invoice service separates its write `JdbcTemplate` from its read `JdbcTemplate` in code. In this H2 POC, both point to the same embedded database by default. H2 embedded file mode does not provide a native primary/physical read-replica topology. A future read copy would need an explicit logical projection or a database platform with native replication; see `docs/READ-REPLICA.md`.

## Event topology

`invoiceflow.domain` is a durable topic exchange. Each downstream service declares its own durable queue. Analytics and read-model queues bind to `#` and receive all event types.

## Transaction handling

Invoice Service commits invoice + outbox row in one DB transaction; scheduled publisher sends unpublished rows and marks them published. This closes the classic DB-commit/message-publish gap for the invoice lifecycle. Downstream handlers use natural business keys (invoice ID, financing ID, payment ID) to suppress repeat effects. Extend inbox tables and transactional outboxes to every service before production.

## Trust boundaries

The POC uses open CORS, no JWT enforcement, static role switching, local passwords and simulated payment. These are local-demo conveniences only. Before production: OAuth2/OIDC, JWT validation at gateway and services, authorization scopes, TLS, secret management, audit trails, PII minimization, rate limiting, anti-fraud controls and payment-provider integration review.
