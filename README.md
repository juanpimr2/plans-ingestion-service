
# Plans Ingestion Service

Microservice that ingests events/plans from an external provider, persists them in PostgreSQL, and exposes a search API.  
Built with **Hexagonal Architecture** and **SOLID** principles. Database is the **source of truth**; provider calls are resilient and can warm-up the DB.

---

## Quick start

### Requirements
- JDK 21
- Maven 3.9+
- Docker & Docker Compose
- GNU Make

### Run locally
```bash
# 1) Start Postgres (via Docker Compose)
make db

# 2) Run the app
make run
# App on: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui or /swagger-ui/index.html
# OpenAPI JSON: http://localhost:8080/v3/api-docs

# 3) Run tests
make test
```

### Configuration (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fever
    username: postgres
    password: postgres

fever:
  provider:
    base-url: https://provider.code-challenge.feverup.com/api/events
    connect-timeout-ms: 5000
    read-timeout-ms: 5000
  search:
    warmup-ms: 400   # time budget for warm-up orchestration

resilience4j:
  circuitbreaker:
    instances:
      provider:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      provider:
        max-attempts: 3
        wait-duration: 300ms
```

---

## API

### `GET /search`
**Query params**
- `starts_at` — ISO‑8601 instant (UTC), e.g. `2021-06-30T20:00:00Z`
- `ends_at` — ISO‑8601 instant (UTC), e.g. `2021-06-30T23:00:00Z`

**Curl**
```bash
curl -s 'http://localhost:8080/search?starts_at=2021-06-30T20:00:00Z&ends_at=2021-06-30T23:00:00Z'
```

**Success (200)**
```json
{
  "data": {
    "events": [
      {
        "id": "291",
        "title": "Camela en concierto",
        "start_date": "2021-06-30",
        "start_time": "21:00",
        "end_date": "2021-06-30",
        "end_time": "22:00",
        "min_price": 15.0,
        "max_price": 30.0
      }
    ]
  },
  "error": null
}
```

**Errors**
```json
// 400 Bad Request
{ "data": null, "error": { "code": "400", "message": "The request was not correctly formed (missing required parameters, wrong types...)" } }

// 404 Not Found
{ "data": null, "error": { "code": "404", "message": "No plans were found for the specified time window." } }

// 500 Internal Server Error
{ "data": null, "error": { "code": "500", "message": "An unexpected error occurred." } }
```

**Contract notes**
- On success, `data.events` contains the list. On error, `data` is `null` and `error` includes `{code, message}`.

---

## Architecture

**Layers / packages**
- `domain.*` — entities and business services (pure Java, framework‑free).
- `application.*` — use cases/orchestration (`SearchPlansUseCase`, `RefreshPlansUseCase`, `SearchWithWarmupUseCase`).
- `adapters.in.rest.*` — controllers, DTOs, mappers, and global exception advice.
- `adapters.out.persistence.*` — JPA repository adapter and mappers.
- `adapters.out.provider.*` — provider client (WebClient) + DTOs + mappers.
- `config.*` — configuration beans (HTTP client, OpenAPI, etc.).

**Ports & Adapters**
- `PlanRepositoryPort` (domain) ⇄ `PlanRepositoryAdapter` (out/persistence).
- `ProviderClientPort` (domain) ⇄ `WebClientProviderClient` (out/provider).

**Resilience**
- Provider client guarded by **Resilience4j**: `@CircuitBreaker` + `@Retry` with a fallback returning an empty list.
- Timeouts at HTTP client level.

**Warm-up**
- Search endpoint always answers from DB.
- When needed, orchestration can trigger a refresh with a budget `fever.search.warmup-ms` (non-blocking or short-blocking).

---

## Persistence

**Entity**
- `plans` table stores provider plans and keeps historical data.
- Fields: provider_id, title, sell_mode, starts_at, ends_at, min_price, max_price, first_seen_at, last_seen_at, currently_available.

**Indexes**
- `(starts_at, ends_at)`
- `(sell_mode)`
- Optional composite `(sell_mode, starts_at, ends_at)` to speed overlap queries.

**Migrations**
- Managed by **Flyway** in `src/main/resources/db/migration`.

---

## Project structure (excerpt)

```
src/main/java/com/fever/challenge/plans
├─ adapters
│  ├─ in
│  │  └─ rest
│  │     ├─ PlanController.java
│  │     ├─ dto/ (EventDto, SearchResponseDto)
│  │     ├─ mapper/ (EventDtoMapper)
│  │     └─ advice/ (RestApiExceptionHandler)
│  └─ out
│     ├─ persistence
│     │  ├─ entity/ (PlanEntity)
│     │  ├─ repo/ (PlanRepository)
│     │  └─ adapter/ (PlanRepositoryAdapter)
│     └─ provider
│        ├─ WebClientProviderClient.java
│        ├─ dto/ (ProviderEventDto, ProviderResponseDto)
│        └─ mapper/ (ProviderEventMapper)
├─ application
│  ├─ search/ (SearchPlansUseCase, impl)
│  ├─ refresh/ (RefreshPlansUseCase, impl)
│  └─ orchestration/ (SearchWithWarmupUseCase, impl)
├─ domain
│  ├─ model/ (Plan, ErrorCode, ErrorDescription)
│  ├─ port/ (PlanRepositoryPort, ProviderClientPort)
│  └─ service/ (PlanService)
└─ config (HttpClientConfig, OpenApiConfig, DomainConfig)
```

---

## Makefile targets

| Target | Use |
|---|---|
| `make db` | Start local PostgreSQL |
| `make run` | Run Spring Boot app |
| `make test` | Run tests |
| `make stop` | Stop PostgreSQL |
| `make clean` | Maven clean |

---

## Testing

- **Unit**: services and mappers.
- **Integration**: controller (MockMvc), repository (H2), provider client (mocked WebClient).
```bash
make test
```

---

## Notes & decisions

- Provider mapping isolated in **out/provider** with DTOs and MapStruct mapper.
- Domain remains framework‑free.
- Global exception handler in **in/rest/advice** maps exceptions → API error contract.
- Consistent naming: avoid `Impl` in adapters (use `*Adapter`, `*Client`).

---

## License

MIT – use freely for evaluation and improvement.
