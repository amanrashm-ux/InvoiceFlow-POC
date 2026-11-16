# Kubernetes migration (later phase)

Do not add Kubernetes to the first two-day POC. Migration outline:
- one Deployment + ClusterIP Service per independently deployable application;
- ConfigMaps for non-secret configuration and Secrets/external secret manager for credentials;
- liveness/readiness probes from Spring Boot Actuator;
- requests/limits and HPA for stateless Java services;
- managed PostgreSQL and RabbitMQ preferred; otherwise use supported operators/StatefulSets;
- Spark on Kubernetes or a managed Spark runtime; durable object storage for event landing;
- Snowflake remains an external service;
- NetworkPolicies, service accounts, TLS, PodDisruptionBudgets and observability.
