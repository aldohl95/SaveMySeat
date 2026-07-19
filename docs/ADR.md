# ADR-001: Concurrency Control for Hold Creation and Checkout

## Status

Accepted — 2026-07-08

## Context

The platform must prevent overselling when multiple users attempt to reserve or purchase tickets for the same event simultaneously. Inventory is managed at the ticket tier level (for example, General Admission or VIP), where each tier represents a quantity of tickets rather than individually assigned seats.

Temporary holds reserve inventory during checkout while ensuring expired holds return inventory to the available pool. Multiple concurrent requests may attempt to reserve tickets from the same ticket tier, requiring a concurrency-control mechanism that guarantees inventory cannot become negative while maintaining a responsive checkout experience.

## Decision

PostgreSQL is the source of truth for ticket inventory. Each TicketTier stores materialized inventory counters:

- `capacity`
- `reserved`
- `sold`

The available inventory is defined as:

```
available = capacity - reserved - sold
```

Hold creation executes inside a single database transaction using an atomic conditional UPDATE on the TicketTier row.

```sql
UPDATE ticket_tier
SET reserved = reserved + :qty
WHERE id = :tierId
  AND capacity - reserved - sold >= :qty;
```

If exactly one row is updated, sufficient inventory exists and a Hold record is inserted within the same transaction. If zero rows are updated, insufficient inventory is available and the request is rejected.

Because the conditional UPDATE acquires a row-level lock on the affected TicketTier, concurrent transactions automatically serialize. Waiting transactions re-evaluate the WHERE clause after the lock is released, preventing overselling without requiring application-level locking or retry logic.

The same inventory invariant is maintained throughout the ticket lifecycle. Checkout converts a hold into a completed purchase by atomically decrementing `reserved`, incrementing `sold`, creating the order, and marking the hold as completed within the same transaction. Expired holds are processed by a background sweeper that decrements `reserved` only for holds that have not already been converted into purchases. All inventory-modifying operations acquire the same TicketTier row lock, ensuring the invariant

```
capacity >= reserved + sold
```

is always preserved.

Checkout is exposed as an idempotent endpoint. Clients supply an `Idempotency-Key` header on every checkout request. The server persists each key together with a hash of the request payload and the resulting response in an `idempotency_record` table. If a checkout request arrives with a key that has already been processed, the server returns the stored response without re-executing the transaction. If the same key arrives with a different payload hash, the server returns 422 Unprocessable Entity. This guarantees that retried checkout requests — whether caused by network failures, client double-submits, or payment provider retries — cannot decrement `reserved` or increment `sold` more than once.

## Alternatives Considered

### Alternative 1: Redis as the Source of Truth

Store available inventory and active holds entirely in Redis using atomic operations.

**Rejected because:** Redis is not the durable system of record. A restart or data loss could remove active holds, making it impossible to determine whether a payment should still be honored or inventory should remain reserved.

### Alternative 2: Serializable Isolation for Every Transaction

Execute all hold creation transactions using PostgreSQL's SERIALIZABLE isolation level.

**Rejected because:** Although SERIALIZABLE prevents overselling, conflicting transactions often perform work only to be aborted and retried. Under heavy contention this reduces throughput compared to row-level locking, where waiting transactions simply queue instead of performing work that is later discarded.

### Alternative 3: One Database Row per Ticket

Create one inventory row for every ticket.

**Rejected because:** The platform manages quantity-based ticket tiers rather than individually identifiable tickets. Creating one row per ticket increases storage requirements and complexity without providing additional value.

### Alternative 4: Optimistic Locking with a Version Column

Add a `version` column to TicketTier and use optimistic concurrency control: read the row, compute the new state in application code, and commit with a `WHERE version = :expectedVersion` guard.

**Rejected because:** Contention on hot ticket tiers is expected to be frequent rather than rare, and optimistic locking degrades to a busy-retry loop under sustained contention. Pessimistic row locks queue waiters efficiently at the database level, whereas optimistic locking forces the application to perform work that is discarded on conflict. Optimistic locking would be the correct choice if contention were rare and writer latency mattered more than throughput; that is not the workload profile of a popular event going on sale.

## Consequences

### Positive

- Prevents overselling through a single atomic database operation.
- PostgreSQL remains the authoritative source of truth for inventory.
- Inventory state remains durable across application or cache failures.
- Checkout validates durable hold records before creating orders.
- The concurrency mechanism relies on standard PostgreSQL transaction semantics without distributed locking.

### Negative

- Concurrent reservations for the same ticket tier serialize on a single database row, increasing latency during high contention.
- Transactions must not hold the ticket tier lock while waiting on external systems such as payment providers, requiring inventory updates to remain short-lived.
- Orders containing multiple ticket tiers acquire ticket tier row locks in ascending order of `ticket_tier.id`. This ordering is enforced at the service layer by sorting the requested tiers before issuing any conditional UPDATE. Applied uniformly across hold creation, checkout, and the sweeper, this rule eliminates the standard AB/BA deadlock cycle between concurrent multi-tier operations.
- Materialized inventory counters (`reserved` and `sold`) must remain synchronized across hold creation, checkout, and expiration.

## When This Design Would Need to Change

This approach is appropriate for small and medium-sized venues where contention on a single ticket tier remains manageable.

For significantly larger events with tens of thousands of simultaneous reservation requests, a single TicketTier row may become a contention hotspot. At that scale, inventory could be partitioned across multiple inventory shards (for example, multiple inventory counter rows selected using consistent hashing), allowing reservation requests to be distributed across several database rows instead of serializing on a single row.

## Implementation Notes

Hold creation is implemented as a Spring service method annotated with `@Transactional` using PostgreSQL's default READ COMMITTED isolation level. Correctness depends on row-level locking acquired by the conditional UPDATE, rather than on a stronger isolation level.

The transaction performs the conditional inventory update followed by insertion of the Hold record. If no row is updated, the transaction rolls back and the API returns 409 Conflict, indicating insufficient inventory.

Checkout performs the inventory transition by decrementing `reserved`, incrementing `sold`, creating the Order and OrderItem records, and marking the Hold as completed within the same transaction.

Expired holds are released by a background sweeper. Each candidate hold is processed inside its own transaction. The sweeper first executes a conditional UPDATE on the hold row:

```sql
UPDATE hold
SET status = 'EXPIRED'
WHERE id = :holdId
  AND status = 'ACTIVE'
  AND expires_at <= now();
```

If zero rows are updated, the hold has already been converted into a completed purchase or expired by a prior sweep, and the transaction commits without touching inventory. If exactly one row is updated, the sweeper acquires the corresponding TicketTier row lock and decrements `reserved` within the same transaction. Because the checkout transaction and the sweeper transaction both mutate the hold row, they cannot both succeed for the same hold: whichever transaction commits first causes the other's conditional UPDATE to match zero rows.

Availability information cached in Redis is invalidated only after a successful database commit using Spring's transaction synchronization callbacks, ensuring cached inventory never reflects an uncommitted transaction.

Although consistent lock ordering eliminates the primary source of deadlocks, PostgreSQL may still raise `40P01` (deadlock detected) or `40001` (serialization failure) from unrelated code paths. Hold creation and checkout are wrapped in an application-level retry policy that catches these errors, applies exponential backoff with jitter, and retries up to three times before surfacing the failure to the client as 503 Service Unavailable. Retries are safe because both operations are idempotent: hold creation is guarded by the conditional inventory UPDATE, and checkout is guarded by the `Idempotency-Key` mechanism described above.

# ADR-002: Primary Key Strategy

## Status

**Accepted** — 2026-07-18

## Context

The ticket reservation system will use a single PostgreSQL relational database to manage users, events, ticket inventory, holds, and orders. The primary system requirement is maintaining correctness during concurrent reservations while keeping the data model efficient and simple.

The application is currently designed as a single Spring Boot service backed by one PostgreSQL database. It does not require distributed ID generation, multiple independent databases, or offline record creation. The expected scale is small venue usage with low thousands of concurrent users.

Primary keys will be heavily referenced through foreign keys between tables, meaning the identifier strategy affects index size, query performance, storage efficiency, and future system flexibility. Some identifiers, such as orders and reservation holds, may eventually appear in external APIs, so the design must account for the tradeoff between internal efficiency and external identifier exposure.

## Decision

We will use **database-generated `BIGINT` sequential primary keys** for all entities.

PostgreSQL sequences will generate identifiers, using Hibernate sequence allocation to improve insert performance by allowing identifier pre-allocation and preserving insert batching capabilities.

These IDs will be used for foreign key relationships and are not intended for external exposure.

## Alternatives Considered

### Alternative 1: UUIDv4 Primary Keys

UUIDv4 provides randomly generated globally unique identifiers.

**Benefits considered:**

- IDs can be generated independently without database coordination.
- Database merges are easier because identifier collisions are extremely unlikely.
- IDs are difficult to guess when exposed publicly.
- Records can receive identifiers before database insertion.

**Rejected because:**

The current architecture uses a single PostgreSQL database, so decentralized identifier generation is not required.

UUIDv4 primary keys increase storage requirements because UUIDs are 16 bytes compared to `BIGINT`'s 8 bytes. This results in larger primary key indexes and larger foreign key columns throughout the database. Random UUID insertion can also create less efficient B-tree index behavior because new values are not sequential.

The benefits UUIDv4 provides are valuable for distributed systems, but those requirements do not currently exist.

### Alternative 2: UUIDv7 Primary Keys

UUIDv7 provides globally unique identifiers while maintaining timestamp ordering, reducing many of the indexing disadvantages of UUIDv4.

**Benefits considered:**

- Provides globally unique identifiers.
- Better database insertion characteristics than random UUIDs.
- Supports future distributed architectures.

**Rejected because:**

Although UUIDv7 improves UUID storage and indexing behavior, the system currently operates within a single PostgreSQL database.

The additional storage cost and larger foreign keys remain, while the primary benefits of UUIDv7 are not required at the current project scale.

### Alternative 3: BIGINT Primary Keys With Separate Public UUID Identifiers

This approach keeps `BIGINT` as the internal database identifier while adding a separate UUID column for external-facing identifiers — for example, an `orders` table with `id BIGINT PRIMARY KEY` alongside `public_id UUID UNIQUE NOT NULL`.

**Benefits considered:**

- Maintains efficient internal joins and indexes.
- Prevents exposing sequential identifiers through APIs.
- Provides a future migration path without replacing internal keys.

**Rejected because:**

Adopting this pattern immediately would introduce additional schema complexity and require maintaining two identifiers across every entity.

It would also require a translation layer at every API boundary from public UUIDs to internal `BIGINT` IDs, even though no current consumer requires public identifiers.

The long-term migration benefit is real, but at the current project stage it introduces additional complexity before the requirement exists. Following a YAGNI approach, this design will only be introduced if external identifier exposure becomes a meaningful requirement.

## Consequences

### Gains

**Smaller and more efficient indexes.** `BIGINT` keys create narrower indexes compared to UUID keys. This improves index density, allowing more index data to remain in memory and improving database cache efficiency.

**Efficient inserts.** Sequential identifiers work well with PostgreSQL B-tree indexes because new records are generally appended rather than inserted randomly.

**Efficient relationships.** Foreign keys remain compact, improving storage efficiency and join performance across highly related tables — for example, the chain from `users` to `holds` to `orders` to `tickets`.

**Easier debugging.** Numeric identifiers are easier to read in logs, SQL queries, and development environments. A log line reading `Order creation failed: order_id=15243` is easier to investigate than an equivalent UUID value.

### Accepted Costs

**Predictable identifiers.** Sequential identifiers can be guessed if exposed publicly — a user seeing `/orders/1001` may infer that `/orders/1002` and `/orders/1003` also exist. This is accepted because primary keys are not considered security boundaries; see the corresponding obligation on structural authorization below.

**German tank problem (business information leakage).** Sequential IDs reveal approximate creation order and volume. An observer placing an order numbered 5000 today and another numbered 5250 next week can estimate order volume, growth rate, and business activity over that window. This is accepted because the system currently operates at a scale where this information exposure is not considered a significant risk.

**Database dependency for ID generation.** Because identifiers are generated by PostgreSQL, records cannot receive their final database identifier without database interaction. This limits scenarios such as offline record creation or independent service-generated identifiers.

### Obligations

**Authorization checks must be enforced structurally.** Because identifiers are predictable and may appear in URLs, resource access cannot rely on obscurity. Every endpoint that retrieves resources by ID must perform ownership and authorization checks in the service layer — users may only access their own orders, users may only modify their own holds, and unauthorized resources must fail closed. Responses should avoid confirming resource existence where appropriate by returning `404 Not Found` instead of `403 Forbidden`.

**Idempotency must use separate identifiers.** Because primary keys are generated by the database, clients cannot use the database ID as an idempotency key before record creation. Operations such as payment processing and checkout must use separate caller-generated idempotency keys — for example, a `payment_request_id` of type UUID supplied by the client, distinct from the eventual `order_id` of type `BIGINT` assigned by the database.

## Revisit Triggers

**Identity generation requirements change.** *Trigger:* The system evolves into multiple services, regions, or databases where identifiers must be generated independently — for example, separate reservation services by region, database sharding, or independent services creating records. *Potential change:* Adopt UUIDv7 or another distributed ID generation strategy.

**External identifier exposure becomes significant.** *Trigger:* Orders, reservations, or events become publicly exposed at a scale where sequential identifiers reveal sensitive business information or create unacceptable enumeration risk. *Potential change:* Introduce a public UUID identifier column while keeping `BIGINT` as the internal key, and expose the UUID through APIs while maintaining existing database relationships.

**Offline record creation becomes required.** *Trigger:* A client or service must create records without contacting the primary database — for example, a mobile organizer application drafting events while disconnected, syncing later. *Potential change:* Move toward client-generated identifiers such as UUIDs.