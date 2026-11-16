# H2 database configuration and troubleshooting

- Every JDBC-backed service uses the H2 2.x runtime driver and a separate H2 file database.
- Default local URL for Invoice Service: `jdbc:h2:file:./data/invoice_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE`. This is an H2 database; `MODE=PostgreSQL` only enables H2 SQL compatibility behavior.
- Username: `sa`; password: blank for local POC only.
- Docker Compose mounts a dedicated named volume at `/data` in each service container.
- H2 console is enabled at `/h2-console` for local development and restricted to local access.

If IntelliJ still reports a connection attempt to `localhost:5432`, open Run/Debug Configurations and remove any manually configured `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, or `SPRING_DATASOURCE_PASSWORD` values that point to the old database. Reload Maven projects so the H2 runtime dependency is downloaded.

Run Invoice Service locally from its own folder:

```bash
cd services/invoice-service
mvn -U clean spring-boot:run
```

If you use H2 in-memory mode instead of persistent local files, set `SPRING_DATASOURCE_URL=jdbc:h2:mem:invoice_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`. Data is lost when the JVM exits.

H2 is for this POC and local demo. Its embedded mode does not offer automatic physical read replicas or production multi-node HA. The current Invoice Service read datasource uses the same H2 database as writes.
