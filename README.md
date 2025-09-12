# Plans Ingestion Service

Microservice for integrating external provider plans into the Fever marketplace.

The project has been designed as a **long-term maintainable system**, applying **hexagonal architecture** and **SOLID principles**.  
It integrates plans from an XML provider, persists them into PostgreSQL, and exposes a REST endpoint to search plans efficiently.

---

## Features

- Fetches plans from external provider (XML feed).
- Stores plans in PostgreSQL with historical persistence:
  - Plans that disappear from provider responses remain retrievable.
- Exposes `/search` endpoint with filtering by time window.
- Resilient provider calls using **Resilience4j** (retries, timeouts).
- Designed for high availability: database is always the source of truth.
- Supports **Flyway migrations** for schema evolution.
- Comprehensive test suite (unit + integration).

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.5**
- **Spring Data JPA + PostgreSQL**
- **Flyway** for DB migrations
- **WebClient** for provider integration
- **Resilience4j** for retries/timeouts
- **Docker Compose** for local PostgreSQL
- **JUnit 5 / Mockito** for testing
- **Lombok & MapStruct** for clean DTOs and mappings

---

## Endpoints

### `GET /search`

**Parameters:**
- `starts_at` – ISO8601 timestamp (UTC)
- `ends_at` – ISO8601 timestamp (UTC)

**Example:**

```bash
curl --location   'http://localhost:8080/search?starts_at=2021-06-30T20:00:00Z&ends_at=2021-06-30T23:00:00Z'
```

**Response:**
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
```

---

## Development

### Requirements
- JDK 21
- Maven 3.9+
- Docker & Docker Compose
- GNU Make

### Running with Makefile

The repository includes a **Makefile** to simplify common operations.

| Target           | Description                                    |
|------------------|------------------------------------------------|
| `make db`        | Start PostgreSQL container (`docker-compose`)  |
| `make stop`      | Stop PostgreSQL container                      |
| `make run`       | Run the Spring Boot application with Maven     |
| `make test`      | Run the full test suite                        |
| `make clean`     | Clean compiled artifacts (`mvn clean`)         |

**Example:**

```bash
# Start DB
make db

# In a separate terminal: run the app
make run

# Run tests
make test
```

The PostgreSQL container runs with:
- Host: `localhost`
- Port: `5432`
- DB: `fever`
- User: `postgres`
- Password: `postgres`

---

## Database

Schema managed with **Flyway** migrations in `src/main/resources/db/migration`.

Initial table `plans` contains:
- Provider ID
- Title
- Sell mode
- Start / End timestamps
- Min / Max price
- First seen / Last seen
- Availability flag

Indexes:
- `(starts_at, ends_at)`
- `(sell_mode)`

---

## Testing

```bash
make test
```

Covers:
- Unit tests for services TODO: test for mappers
- Integration tests for persistence (H2)
- Integration tests for provider client (mocked & real)

---

## Design Considerations

- **Hexagonal architecture**:
  - `adapters.in.rest` for controllers
  - `application.*` for use cases
  - `adapters.out.*` for persistence & provider
  - `domain.*` for pure business logic
- **SOLID principles**:
  - Clear separation of use cases (query, refresh, orchestration)
  - Repository & Provider as **ports**, injected via adapters
- **Resilience**:
  - Provider calls retried & timed out
  - Database is the primary source of truth
- **Performance**:
  - Search endpoint always answers from DB (hundreds of ms SLA)
  - Async refresh keeps DB updated in the background

---

## Future Enhancements

- Batch upsert for higher ingestion throughput
- Caching layer for hot queries
- Metrics and tracing with Micrometer
- Load testing for high-traffic scenarios (5k-10k req/s)

---

## License

MIT – use freely for evaluation and improvement.
