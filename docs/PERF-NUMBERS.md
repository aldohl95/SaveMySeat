# Performance Numbers

Measured on a laptop, single Postgres instance, default HikariCP pool (10 connections).

## Login
- 5 VUs: 74 rps, p95 62ms
- 20 VUs: 157 rps, p95 125ms
- Bottleneck: BCrypt work factor 10 (~100ms per verify)

## Hold Creation (Single Tier, Contended)
- 10 VUs on 1 tier: 2,678 rps aggregate, p95 4ms
- Write-path p95: 8.9ms
- Contended on tier row's PESSIMISTIC_WRITE lock

## Hold Creation (Multi-Tier, Distributed)
- 10 VUs on 21 tiers: 4,350 rps aggregate, p95 2ms
- Write-path p95: 3.3ms
- ~62% higher throughput than single tier, ~63% lower write-path latency
- Demonstrates lock contention as the throughput ceiling