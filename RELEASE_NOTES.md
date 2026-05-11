# Release notes

## 0.0.1-SNAPSHOT (enterprise upgrade track)

### Highlights

- Spring Boot **3.3.7**, Web3j **4.13.0**, Java **17**.
- **JWT (HS256)** OAuth2 Resource Server for `/api/**`; OpenAPI documents Bearer scheme.
- **Redis** caching for event pagination; optional Redis for idempotency keys (memory fallback when Redis auto-config is absent).
- **ABI JSON** loading and optional **ERC-721 / ERC-1155** event profiles; `ContractProfilePlugin` SPI for extra definitions.
- **Alerting** (email + Slack) with cooldown; **lottery_indexer_errors_total** Micrometer counter by stage (`poll`, `ws`, `ingest`).
- **Management server** on port **9090** (health + Prometheus); main API on **8081**.
- **Dockerfile**, extended **docker-compose**, **Helm** chart with example values for Sepolia, BSC testnet, Polygon Amoy.
- **DDD-style** packages: `application`, `infrastructure`, `interfaces`, `domain`.

### Breaking changes

- REST API now requires a valid **JWT** for `/api/**` (Swagger UI and OpenAPI docs remain under `/v3/api-docs`, `/swagger-ui` — permitlisted).
- `POST /api/v1/listener/reset` requires header **`Idempotency-Key`** (replay protection).

### Operational notes

- Set `lottery.chain.listeners-enabled=false` for tests or maintenance (disables polling + WS ingestion startup paths).
- JaCoCo gate uses exclusions for generated-style packages; raise toward **80%** with WireMock / Testcontainers JSON-RPC tests.

### Upgrade steps

1. Provide Redis and JWT secret in environment / Helm values.
2. Run database migrations (Flyway) against MySQL before starting new version.
3. Issue JWTs compatible with `app.security.jwt.issuer` and `audience`.
