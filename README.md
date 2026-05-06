# blockchain-lottery-java-event-indexer

Java 17, Spring Boot **3.3.7**, and **Web3j 4.13** service that ingests EVM **lottery** (and optional **ERC-721 / ERC-1155**) events, persists them in **MySQL**, caches reads in **Redis**, exposes a **JWT (HS256)**-protected REST API, and publishes **Prometheus** metrics. Designed for **one chain per deployment** (Ethereum, BSC, Polygon, or any EVM L2) with distinct Helm values or environment variables.

## Features

- Dual ingestion: HTTP polling backfill + optional WebSocket live logs; checkpoint + reorg handling via `listener_state`.
- Event decoding from YAML signatures, optional **ABI JSON** (`lottery.chain.abi-json-path`), optional ERC standards; **plugin** hooks via `ContractProfilePlugin`.
- **Redis** cache for paginated event queries; API **address masking** (`app.api.mask-addresses`).
- **Idempotency-Key** header on `POST /api/v1/listener/reset` with Redis-backed replay protection (in-memory fallback when Redis is off).
- **Alerts**: email + Slack webhook (`lottery.alerting.*`) with cooldown; Micrometer **lottery_indexer_errors_total** by stage.
- **Observability**: main app on `8080`, **management** on `9090` with `/actuator/health/*` and `/actuator/prometheus`.
- **Packaging**: multi-stage [deploy/Dockerfile](deploy/Dockerfile), [docker-compose.yml](docker-compose.yml), [Helm chart](deploy/helm/blockchain-lottery-indexer).

## Quick start (local)

1. Start MySQL + Redis: `docker compose up -d mysql redis`
2. Copy [.env.example](.env.example) to `.env` and set `CHAIN_*`, `JWT_SECRET`, and DB credentials.
3. Run: `mvn spring-boot:run`
4. **API** (port 8080): `http://localhost:8080/v3/api-docs` — OpenAPI 3 with JWT security scheme.
5. **Metrics** (port 9090): `http://localhost:9090/actuator/prometheus` (scrape from your cluster network; do not expose publicly without auth).

### Docker (all-in-one)

```bash
docker compose up --build
```

## API authentication (HS256)

Configure `app.security.jwt.secret` (long random), `issuer`, and `audience`. Call protected endpoints with:

`Authorization: Bearer <JWT>`

Example claims: `iss` = `blockchain-lottery-indexer`, `aud` = `api` (or array containing `api`).

**Example (list events):**

```bash
export TOKEN="eyJ..."   # mint with your tool; must match issuer/audience/secret
curl -sS -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/events?page=0&size=10"
```

**Reset listener (requires Idempotency-Key):**

```bash
curl -sS -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  "http://localhost:8080/api/v1/listener/reset?fromBlock=1000"
```

## Configuration highlights

| Area | Properties |
|------|------------|
| Chain | `lottery.chain.rpc-url`, `ws-url`, `contract-address`, `additional-contract-addresses`, `chain-id`, `confirmations`, `max-blocks-per-poll`, `listeners-enabled` |
| ABI | `lottery.chain.abi-json-path` (classpath or `file:`) |
| ERC | `enable-erc721-transfer`, `enable-erc1155-transfer-single`, `enable-erc1155-transfer-batch` |
| Security | `app.security.jwt.*`, `app.api.mask-addresses` |
| Redis | `spring.data.redis.*`, `spring.cache.*` |
| Alerts | `lottery.alerting.enabled`, email/slack sub-keys |

Per-chain deployment examples: [values-ethereum-sepolia.yaml](deploy/helm/blockchain-lottery-indexer/values-ethereum-sepolia.yaml), [values-bsc-testnet.yaml](deploy/helm/blockchain-lottery-indexer/values-bsc-testnet.yaml), [values-polygon-amoy.yaml](deploy/helm/blockchain-lottery-indexer/values-polygon-amoy.yaml).

## Architecture & deployment

- [docs/architecture.md](docs/architecture.md) — data flow and component diagram (Mermaid).
- [docs/deployment.md](docs/deployment.md) — Helm, Prometheus scrape, secrets.

## Monitoring

- Import [monitoring/grafana-dashboard.json](monitoring/grafana-dashboard.json) into Grafana (adjust datasource UID).
- Key metrics: `lottery_event_ingested_total`, `lottery_indexer_errors_total`, JVM/process metrics from Micrometer.

## Testing & coverage

```bash
mvn verify
```

JaCoCo line coverage gate is configured in `pom.xml` (domain/DTO packages excluded from denominator). Add JSON-RPC integration tests to push toward **80%+** overall.

## Demo / employer bundle

- Testnet RPC URLs and contract addresses: set in each Helm values file (placeholders `YOUR_*`).
- Screenshots: add under `docs/images/` (Grafana dashboard, Swagger UI, sample API response).

## Release notes

See [RELEASE_NOTES.md](RELEASE_NOTES.md).

## License

See [LICENSE](LICENSE).
