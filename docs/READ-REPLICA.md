# Read/write separation with H2

The Invoice Service keeps separate write and read `JdbcTemplate` beans so query routing can be introduced without changing controller code. In the current H2 POC, the read template points to the same H2 `DataSource` as the write template. This avoids a misleading second database that would contain stale or missing rows.

H2 embedded file mode does not provide automatic primary-to-replica replication. A second H2 file is not a read replica unless you build and operate a synchronization mechanism. If separate read storage is required while staying on H2, implement an explicit event-driven read projection (CQRS) and accept eventual consistency.
