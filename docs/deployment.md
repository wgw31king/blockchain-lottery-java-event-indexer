# Deployment guide

## Helm

Install from the chart directory (replace release name and namespace):

```bash
helm upgrade --install lottery-indexer-sepolia ./deploy/helm/blockchain-lottery-indexer \
  -f ./deploy/helm/blockchain-lottery-indexer/values.yaml \
  -f ./deploy/helm/blockchain-lottery-indexer/values-ethereum-sepolia.yaml \
  --set secrets.jwtSecret="$JWT_SECRET" \
  --set secrets.mysqlPassword="$MYSQL_PASSWORD"
```

### Secrets

- **JWT_SECRET**: HS256 signing key (long random string).
- **MySQL**: prefer an external managed database; wire host/user via `values.yaml` and password via `secrets.mysqlPassword`.

### Prometheus

- Scrape the **management** port (`9090` by default): path `/actuator/prometheus`.
- If you use Prometheus Operator, enable `serviceMonitor.enabled` and ensure the Service exposes the `management` port name.

### Health probes

The chart uses Spring Boot 3 liveness/readiness endpoints on the management port:

- `/actuator/health/liveness`
- `/actuator/health/readiness`

Require `management.endpoint.health.probes.enabled=true` (enabled in `application.yml`).

## Docker Compose

For local or single-node demos:

```bash
docker compose up --build
```

Override chain endpoints via environment (see [docker-compose.yml](../docker-compose.yml)).

## Network hardening

- Do not expose port `9090` on public ingress; keep Prometheus scraping internal.
- Rotate JWT secrets per environment; use separate databases or schemas per chain when running multiple indexers.
