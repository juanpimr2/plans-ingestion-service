# Fever Plans Ingestion Service

## The challenge

This microservice integrates plans from an **external provider** into the Fever marketplace.
The external provider exposes an XML API at: https://provider.code-challenge.feverup.com/api/events

Key requirements:

- Expose a single **`/search`** endpoint.
- Accept `starts_at` and `ends_at` parameters (ISO-8601, UTC).
- Return only plans available within that time range and with `sell_mode="online"`.
- **Past plans must remain retrievable**, even if they disappear from subsequent provider responses.
- Must remain **performant** (hundreds of ms) and **resilient** (work even if provider is down).

---

## Design choices

- **Hexagonal Architecture (Ports & Adapters)**:
  - **Domain**: `Plan` model, ports for provider and repository, and `PlanService` (pure Java, no Spring).
  - **Application**: use cases grouped by responsibility:
    - `SearchPlansUseCase`
    - `RefreshPlansUseCase`
    - `SearchWithWarmupUseCase` (orchestrates refresh + DB query).
  - **Adapters**:
    - REST in: `PlanController`
    - Provider out: `WebClientProviderClient` (XML parsing, timeout + retry).
    - Persistence out: `PlanRepositoryAdapter` (JPA + Flyway migrations).

- **Persistence**:
  - PostgreSQL with Flyway migrations (`db/migration/V1__init.sql`).
  - `upsert` logic ensures plans are updated or inserted by `provider_id`.
  - Past plans stay in DB even if removed from provider.

- **Resilience**:
  - Provider calls via `WebClient` with **3s timeout** + **retry** (Resilience4j).
  - Calls are triggered **asynchronously**, so `/search` always responds from DB.

- **Performance**:
  - `/search` always queries DB → constant latency, not dependent on provider.
  - Asynchronous ingestion ensures fresh data is attempted without blocking.

---

## API Spec

### GET `/search`

**Parameters**
- `starts_at` → required, e.g. `2021-06-30T20:00:00Z`
- `ends_at` → required, e.g. `2021-06-30T23:00:00Z`

**Response**
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
  }
}

