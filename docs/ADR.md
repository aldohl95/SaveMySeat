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
