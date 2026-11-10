# SaveMySeat

A ticketing backend that handles concurrent ticket sales without overselling.

Built in Spring Boot 4 with Postgres 16 and Java 25, with a React + TypeScript frontend. Full purchase lifecycle: registration, JWT auth with refresh token rotation, venue and event management, and a two-phase hold-then-purchase model verified by an integration test that spawns 50 concurrent threads at a 10-ticket tier and asserts exactly 10 succeed.

## Live Demo

- **API (Render):** https://savemyseat.onrender.com — Swagger UI at [`/swagger-ui.html`](https://savemyseat.onrender.com/swagger-ui.html), health at [`/actuator/health`](https://savemyseat.onrender.com/actuator/health)
- **Frontend (Netlify):** _TODO: add Netlify URL_

> The backend runs on Render's free tier and sleeps when idle, so the first request after a quiet period can take 30-60 seconds while it wakes up.

<!-- TODO: add a screenshot or GIF of browse -> hold -> pay -> My Orders -->

[![CI](https://github.com/aldohl95/SaveMySeat/actions/workflows/ci.yml/badge.svg)](https://github.com/aldohl95/SaveMySeat/actions/workflows/ci.yml)

## Highlights

- **Concurrency-safe inventory** via row-level pessimistic locking on the ticket tier. The crown-jewel test lives in [`HoldServiceConcurrencyTest`](backend/src/test/java/com/savemyseat/hold/HoldServiceConcurrencyTest.java).
- **JWT authentication** with short-lived access tokens and rotating refresh tokens. Reuse of a consumed refresh token triggers a theft signal.
- **Three-layer authorization**: route-level filtering, role-based `@PreAuthorize`, service-level ownership checks. Ownership failures return 404 identical to genuinely-missing resources — no enumeration.
- **Production observability**: structured JSON logs with per-request user correlation via MDC, Prometheus metrics for business events and infrastructure, request timings per URI.
- **Background hold sweeper** that releases expired holds safely against concurrent checkout via a conditional UPDATE on the hold row.
- **Full-stack and deployed**: Dockerized backend on Render with managed Postgres, React + TypeScript SPA on Netlify, configuration entirely via environment variables.
- **Architecture decision record** documenting the significant design decisions ([docs/ADR.md](docs/ADR.md)).
- **Load-tested**: hold creation sustains 4,350 requests/second distributed across tiers on a laptop. Numbers in [`docs/PERF-NUMBERS.md`](docs/PERF-NUMBERS.md).

## The Concurrency Story

The core problem: many people try to buy the last few tickets at the same time. If the system doesn't handle concurrency correctly, oversell happens — two customers pay for the same seat, refunds go out, trust is damaged.

The invariant that must hold: `reserved + sold <= capacity`, always.

Naive check-then-write logic loses this invariant under contention:

```
Thread A: reads reserved=8, sold=0, capacity=10, sees room for 2
Thread B: reads reserved=8, sold=0, capacity=10, sees room for 2
Thread A: writes reserved=10
Thread B: writes reserved=10  ← should have been 12; oversell
```

The defense: acquire an exclusive row lock on the tier row before the read, hold it until the write commits. Postgres serializes concurrent transactions on the same row, so only one transaction can be between "read state" and "write state" at any moment.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT t FROM TicketTier t WHERE t.id = :id")
Optional<TicketTier> findByIdWithLock(@Param("id") Long id);
```

The test that proves it:

```java
@Test
void concurrentHoldsRespectCapacity() {
    // Setup: tier with capacity=10, reserved=0, sold=0
    // 50 threads race to hold 1 ticket each
    // Assert: exactly 10 succeed, tier.reserved == 10, active hold count == 10
}
```

Three independent assertions must all agree for the test to pass. A locking bug that produced overselling would surface as any mismatch between them.

[Full test](backend/src/test/java/com/savemyseat/hold/HoldServiceConcurrencyTest.java) · [Design decisions (ADR)](docs/ADR.md)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       API Layer                             │
│  Auth · Venues · Events · Tiers · Holds · Orders            │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Security Layer                            │
│  JwtAuthenticationFilter → SecurityContext (with MDC)       │
│  @PreAuthorize gates roles                                  │
│  Service body enforces resource ownership                   │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  Domain Services                            │
│  Concurrency-safe via row-level pessimistic locking         │
│  State machine transitions on Order lifecycle               │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Persistence                               │
│  Postgres 16 · Flyway migrations · JPA/Hibernate            │
│  Sequence-based IDs, timestamptz, BIGINT cents              │
└─────────────────────────────────────────────────────────────┘
```

### Package Layout

Feature-based, not layer-based. Each domain owns its entity, repository, service, controller, and DTOs.

```
com.savemyseat/
├── auth/              # JWT, refresh tokens, security context
├── config/            # SecurityConfig, JpaConfig
├── exception/         # GlobalExceptionHandler with ProblemDetail
├── event/             # Event entity + service + controller + dto
├── hold/              # Hold entity + service + controller + sweeper + dto
├── order/             # Order entity + service + controller + dto
├── tickettier/        # TicketTier entity + service + controller + dto
├── user/              # User entity + service + controller + dto
└── venue/             # Venue entity + service + controller + dto
```

## Design Decisions

Significant technical choices (row-level locking, idempotent checkout, the expiration sweeper, multi-tier lock ordering, deferred features) are recorded in [docs/ADR.md](docs/ADR.md).

## API

About 28 endpoints in total. Full OpenAPI spec at `/v3/api-docs`, interactive UI at `/swagger-ui.html` when the app is running.

Key endpoints:

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a new user account |
| POST | `/api/auth/login` | Authenticate, receive access + refresh tokens |
| POST | `/api/auth/refresh` | Rotate refresh token, receive new pair |
| GET | `/api/venues` | List venues (public) |
| POST | `/api/venues` | Create venue (organizer only, owned by creator) |
| GET | `/api/events` | List events (public) |
| POST | `/api/events` | Create event (organizer only, at owned venue) |
| GET | `/api/tickettiers/{id}` | Get tier details (public) |
| GET | `/api/orders` | List the current user's orders (paginated, newest first) |
| POST | `/api/holds` | Reserve tickets under a short TTL |
| DELETE | `/api/holds/{id}` | Release a hold before it expires |
| POST | `/api/orders` | Convert a hold into a pending order |
| POST | `/api/orders/{id}/pay` | Confirm payment, transition to PAID |
| POST | `/api/orders/{id}/refund` | Refund a paid order |
| DELETE | `/api/orders/{id}` | Cancel a pending order |

Order lifecycle:

```
PENDING ──────► PAID ──────► REFUNDED
   │
   ▼
CANCELLED
```

## Performance

Measured on a laptop, single Postgres instance, default connection pool (10).

| Endpoint | Concurrency | Throughput | p95 latency |
|---|---|---|---|
| POST `/api/auth/login` | 5 VUs | 74 req/s | 62ms |
| POST `/api/auth/login` | 20 VUs | 157 req/s | 125ms |
| POST `/api/holds` (single tier) | 10 VUs | 2,678 req/s | 4ms |
| POST `/api/holds` (21 tiers) | 10 VUs | 4,350 req/s | 2ms |

The 62% throughput improvement from single-tier to multi-tier empirically demonstrates the row lock as the throughput ceiling. Full analysis in [PERF-NUMBERS.md](docs/PERF-NUMBERS.md).

Login is bounded by BCrypt at work factor 10 (deliberately slow, security-critical). Hold creation is bounded by the tier row lock (deliberately serializing to prevent oversell).

## Observability

- **Structured logs** in JSON via Logback + logstash-logback-encoder. Every log line inside an authenticated request carries a `userId` field via MDC, making per-session log correlation queryable in log aggregators.
- **Metrics** at `/actuator/prometheus` — HTTP request stats per URI, JVM stats, HikariCP connection pool stats, Spring Data repository invocation timings, scheduled task execution stats. Custom domain counters: `holds_created_total`, `orders_paid_total`, `refresh_tokens_reused_total` (a security metric — spikes indicate credential theft attempts).
- **API docs** at `/swagger-ui.html` for interactive endpoint exploration.
- **Health check** at `/actuator/health`.

Order history (`GET /api/orders`) is served by a composite index on `(user_id, created_at DESC)`. Active-hold lookups use partial indexes so the sweeper and per-user queries only touch live rows.

## Frontend

React + TypeScript (Vite) SPA in [`frontend/`](frontend/): register and login, browse venues and events, hold tickets on a tier, confirm a (simulated) payment, and view order history. Tokens are kept in `localStorage` (a known XSS trade-off versus httpOnly cookies). The API base URL comes from `VITE_API_BASE` at build time.

## Deployment

- **Backend**: multi-stage `backend/Dockerfile` (JDK build stage, JRE runtime stage, non-root user) deployed to Render, connected to Render Postgres through environment variables (`SPRING_DATASOURCE_*`, `JWT_SECRET`, `app.cors.allowed-origins`).
- **Frontend**: Netlify, with `_redirects` for SPA routing.

## Testing

Three layers:

- **Unit tests** verify service logic in isolation.
- **Integration tests** run against a real Postgres via Testcontainers. Includes the concurrency test proving oversell impossibility.
- **Load tests** in [`loadtests/`](loadtests/) via k6, empirically measuring throughput and latency ceilings.

CI runs unit and integration tests on every push via GitHub Actions.

## Getting Started

### Prerequisites

- Java 25
- Docker + Docker Compose
- Maven (or use the wrapper `./mvnw`)

### Run locally

```bash
# Start Postgres
docker compose up -d

# From the backend directory
cd backend

# Set the JWT secret
export JWT_SECRET=$(openssl rand -base64 32)

# Run the app
./mvnw spring-boot:run
```

The API is now at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Run the tests

```bash
cd backend
./mvnw test
```

The concurrency test uses Testcontainers and requires Docker to be running.

### Run the load tests

```bash
# Install k6 first: https://k6.io/docs/getting-started/installation/

cd loadtests
k6 run login-load.js
k6 run hold-load-single-tier.js
k6 run hold-load-multi-tier.js
```

## Known Deferred Features

Documented as intentional trade-offs, not oversights. Each has a designed solution.

- **Real Stripe integration.** Currently `stripe_session_id` is a placeholder string. See ADR-003.
- **Timing-attack defense on login.** Fast fail on unknown emails leaks existence via response time.
- **Rate limiting on auth.** Credential stuffing unmitigated at scale.
- **Refresh token chain invalidation on theft.** Reuse detection rejects the request but doesn't recursively invalidate descendants.
- **Multi-tier orders.** Currently one tier per order; multi-tier requires an `OrderItem` model with sorted lock acquisition per ADR-001.
- **Email verification and password reset.** Both require email infrastructure.
- **Redis inventory caching / rate limiting.** Deliberately not used; the Postgres row lock is the source of truth and the measured ceiling is well above expected load.

## Tech Stack

- **Language**: Java 25
- **Frontend**: React, TypeScript, Vite
- **Hosting**: Render (API + Postgres), Netlify (frontend), Docker
- **Framework**: Spring Boot 4.1
- **Database**: Postgres 16
- **ORM**: JPA / Hibernate
- **Migrations**: Flyway
- **Auth**: jjwt 0.13
- **Metrics**: Micrometer + Prometheus
- **Logs**: Logback + logstash-logback-encoder
- **Testing**: JUnit 5, Testcontainers, k6
- **CI**: GitHub Actions
- **API Docs**: springdoc-openapi

## Repository Structure

```
SaveMySeat/
├── backend/                    # Spring Boot application
│   ├── src/main/java/          # Application code
│   ├── src/main/resources/     # Configuration, migrations
│   ├── src/test/java/          # Tests including concurrency test
│   └── pom.xml
├── frontend/                   # React + TypeScript SPA
├── docs/                       # Design documents
│   ├── ADR.md                  # Architecture Decision Record
│   └── PERF-NUMBERS.md
├── loadtests/                  # k6 load test scripts
├── scripts/                    # Development utility scripts
├── .github/workflows/          # CI configuration
├── docker-compose.yml          # Postgres
└── README.md
```
