# Plans Ingestion Service

Microservice that ingests **plans (events)** from an external **XML** provider, persists them in **PostgreSQL**, and exposes a **fast, DB-backed search** endpoint.  
Built with **Hexagonal Architecture** and **SOLID**. The **database is the source of truth**; provider calls are resilient and never sit on the critical path of reads. Includes a **warm-up orchestration** to avoid cold starts.

---

## Quick start

### Requirements
- JDK 21
- Maven 3.9+
- Docker & Docker Compose
- GNU Make

### Run locally
```bash
# 1) Start Postgres (Docker)
make db

# 2) Run the app
make run
# App:        http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui
# OpenAPI:    http://localhost:8080/v3/api-docs

# 3) Tests
make test
```

---

## Configuration (`application.yml`)

> Values tuned to keep `/search` in **hundreds of ms**, even if the provider is slow or down.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fever
    username: postgres
    password: postgres

fever:
  provider:
    base-url: https://provider.code-challenge.feverup.com/api/events
    connect-timeout-ms: 250     # fail fast on bad networks
    read-timeout-ms: 1000       # socket limit; keep > fetch-timeout
    fetch-timeout-ms: 600       # end-to-end timeout applied in WebClient
  search:
    warmup-ms: 800              # max budget for warm-up orchestration

resilience4j:
  retry:
    instances:
      provider:
        max-attempts: 1         # avoid sequential retries on the read path
        wait-duration: 0ms
  circuitbreaker:
    instances:
      provider:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
```

**Notes**
- `ProviderProperties` **has no defaults** and is `@Validated`: the app **won’t start** unless `base-url`, `connect-timeout-ms` and `read-timeout-ms` are set.
- `HttpClientConfig` wires Reactor Netty with the **connect** and **read** timeouts.
- `WebClientProviderClient` applies `fetch-timeout-ms` as a **hard cap** for each provider call.

---

## API

### `GET /search`
Returns plans that **overlap** the provided time window and that were **ever available with `sell_mode = "online"`**.  
Reads are **always served from the local DB** (independent of provider health).

**Query params**
- `starts_at` — ISO-8601 instant (UTC), e.g. `2021-06-30T20:00:00Z`
- `ends_at` — ISO-8601 instant (UTC), e.g. `2021-06-30T23:00:00Z`

**Curl**
```bash
curl -s 'http://localhost:8080/search?starts_at=2021-06-30T20:00:00Z&ends_at=2021-06-30T23:00:00Z'
```

**Responses**
- **200** – results in `data.events`
- **404** – empty result set
- **400** – bad/missing params
- **500** – unexpected error

Contract is documented in Swagger/OpenAPI.

---

## Design highlights

- **DB-first reads**  
  `/search` never calls the provider on the request path → stable latency independent of external services.

- **Warm-up orchestration (SWR)**  
  Implemented in `SearchWithWarmupUseCaseImpl`:
    - **Cold start (DB empty):** do a **blocking refresh** bounded by `fever.search.warmup-ms`, then read from DB.
    - **Warm path (DB has data):** read **immediately** from DB and trigger a **non-blocking** refresh (stale-while-revalidate) within the same budget.

- **Resilience**  
  Provider client uses **Resilience4j** (`@CircuitBreaker`, `@Retry`) and strict timeouts. Fallback returns **empty** list so the search endpoint remains unaffected.

- **Clean boundaries**  
  Domain is framework-free. Ports: `PlanRepositoryPort`, `ProviderClientPort`. Adapters: JPA & WebClient. Mappers via **MapStruct**.

---

## Persistence

- **Entity / Table:** `plans`
    - `provider_id` (unique), `title`, `sell_mode`
    - `starts_at`, `ends_at` (UTC)
    - `min_price`, `max_price`
    - `first_seen_at`, `last_seen_at` (history)
    - `currently_available` (future use; not used to filter `/search`)
- **Indexes**
    - `(starts_at, ends_at)`
    - `(sell_mode)`
    - `(sell_mode, starts_at, ends_at)` (composite for overlap scans)
- **Migrations:** Flyway under `db/migration`.

---

## Mapping

- **Provider (XML → domain):** `WebClientProviderClient` + DTOs → `ProviderPlanMapper` (aggregates `min_price`/`max_price` from zones).
- **Persistence:** `PlanPersistenceMapper` (`Plan` ⇄ `PlanEntity`).
- **REST:** `EventDtoMapper` (snake_case dates/times as strings per spec).

---

## Testing

- **Unit:** domain services and mappers.
- **Integration:** controller (MockMvc), repository (H2/Postgres), provider client (real/mocked as needed).
```bash
make test
```

---

## Makefile (main targets)

| Target | Use |
|---|---|
| `make db`   | Start local PostgreSQL |
| `make run`  | Run Spring Boot app |
| `make test` | Run tests |
| `make stop` | Stop PostgreSQL |
| `make clean`| Maven clean |

---
