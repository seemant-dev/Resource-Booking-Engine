# Week 4 performance and concurrency test plan

## Purpose
1. Diagnose and correct the booking-history read path using pagination, PostgreSQL query plans, JDBC spans, an evidence-based index decision, and an N+1 fetch-plan fix.
2. Verify the exactly-one-booking invariant and compare optimistic and pessimistic locking under identical contention.

The first measured result is a **pre-fix benchmark**. The final result, recorded after the fixes on a stable commit, becomes the **post-fix baseline**. The earlier 5-VU k6 run remains an instrumentation/sanity result and is not directly comparable because its dataset and workload differ.

## API scope

| Journey                        | Endpoint                                               | Purpose                                                |
| ------------------------------ | ------------------------------------------------------ | ------------------------------------------------------ |
| Browse                         | `GET /api/resources`                                   | Simple-read reference and regression signal            |
| Availability                   | `GET /api/resources/{id}/slots?date=YYYY-MM-DD`        | Filtered/indexed database read                         |
| Booking history                | `GET /api/bookings/me?page=0`                          | Fixed-size pagination, N+1, and query-plan investigation |
| Contention                     | `POST /api/slots/{id}/book`                            | Core business/concurrency experiment                   |
| Login                          | `POST /api/auth/login`                                 | Test setup and smoke check only                        |
| Admin CRUD and slot generation | Functional/integration verification only               | Too infrequent for the Week 4 performance baseline     |
| Cancellation                   | Functional verification only                           | Not required for the two fault stories                 |

## DB load

| Data                                                |       Count |
| --------------------------------------------------- | ----------: |
| Resources (`resource1`–`resource100`)               |         100 |
| Users                                               |         500 |
| Slots                                               |      60,000 |
| Booked slots                                        |      50,000 |
| Available slots                                     |      10,000 |
| Bookings                                            |      50,000 |
| Heavy-user bookings (`user1@mailinator.com`)        |      10,000 |
| Other-user bookings                                 |      40,000 |
| Slots per resource                                  |         600 |
| Booked/available per resource                       |   500 / 100 |

Seed script is idempotent: it deletes and recreates only test data.

## Execution architecture

Virtual users will execute on the local machine so they can reach local SlotEngine without publicly exposing it.

k6 on local machine ──HTTP──> SlotEngine on local machine
        |                            |
        | k6 results                 | OpenTelemetry
        v                            v
Grafana Cloud k6             Grafana Observability


recorded-run command:
k6 cloud run --local-execution .\k6\<script>.js

k6 connectivity to be verified before this output mode is frozen. If streaming is not reliable, every measured before/after run will instead use ordinary local `k6 run`. Execution/output modes must never be mixed inside a comparison.

## Performance dataset

The reproducible performance seed will create at least:

- 50,000 slots.
- 50,000 bookings.
- 100 resources.
- Several hundred users.
- One deliberately heavy test user with approximately 10,000 bookings.

The skewed heavy user is a documented fault-lab condition used to expose unbounded responses, sorting work, and lazy-loading behaviour. The seed must use bulk SQL, remain separate from production Flyway migrations, and preserve the same row distribution for every comparison.

## Standard read workload

The browse/availability and paginated booking-history tests will use the following controlled local workload:

| Setting           |       Value |
| ----------------- | ----------: |
| Warm-up           |  30 seconds |
| Steady load       | 120 seconds |
| Cooldown          |  15 seconds |
| Load              |      10 VUs |
| Measured runs     |           3 |
| Booking page size |          50 |

These values define an assumed portfolio workload, not observed production traffic or production capacity.

## Read targets

| Target                          | Rule                                                     |
| ------------------------------- | -------------------------------------------------------- |
| Unexpected HTTP errors          | `< 1%`                                                   |
| Functional checks               | `100%`                                                   |
| p95 duration                    | `< 500 ms`                                               |
| p99 duration                    | Record without inventing a claim                         |
| Booking response size           | Never exceed the server-fixed page size of 50            |
| JDBC statements after N+1 fix   | Constant/bounded rather than proportional to page size   |

The latency target is an assumed engineering threshold, not a business SLO.

### Isolation stages


Stage A: pagination added, N+1 retained, old index
    -> measure three runs and capture trace/EXPLAIN evidence

Stage B: evidence-based order-supporting index added
    -> repeat the identical three-run workload

Stage C: @EntityGraph or JOIN FETCH added
    -> repeat the identical three-run workload

Stage D: stable final build
    -> record the post-fix baseline


Only one performance variable should change between adjacent stages.

## Concurrency experiment

| Setting    | Value                                    |
| ---------- | ---------------------------------------- |
| Attempts   | 20 simultaneous booking attempts         |
| Target     | One newly created available slot per round |
| Runs       | 3 per locking strategy                   |
| Strategies | Optimistic and pessimistic               |

### Correctness targets per round

| Result                        |     Target |
| ----------------------------- | ---------: |
| `201 Created`                 |  Exactly 1 |
| Expected `409 Conflict`       | Exactly 19 |
| Unexpected responses          |          0 |
| Confirmed database bookings   |  Exactly 1 |
| Lock timeouts/deadlocks       |          0 |

Expected `409` responses are business conflicts and must not be classified as unexpected test failures.

## Measurement protocol

For every before/after comparison:

1. Use the fixed environment recorded in `run-metadata.md`.
2. Record the commit SHA, application version, dataset revision, exact command, and run timestamp.
3. Use the same seed, user, endpoint parameters, k6 script revision, VUs, stages, and output mode.
4. Warm the JVM and database consistently before a measured run.
5. Save all three runs; never retain only the fastest result.
6. Record p50, p95, p99, throughput, checks, and unexpected errors.
7. Capture the matching Grafana trace/JDBC evidence and relevant application metrics.
8. For SQL work, save `EXPLAIN (ANALYZE, BUFFERS)` output including scan type, actual rows, loops, sort, buffers, and execution time.
9. For contention, query the database after each round to prove exactly one confirmed booking.
10. Report actual numbers and limitations without claiming production capacity.

## Evidence required for completion

- Complete k6 summaries for all measured runs.
- Before/after Grafana traces or trace identifiers showing JDBC behaviour.
- Before/after PostgreSQL execution plans.
- Measured JDBC statement counts for the booking-history page.
- Database correctness results for every contention round.
- Optimistic/pessimistic comparison table.
- One completed slow-SQL/N+1 fault card.
- One completed concurrency fault card.
- A final stable post-fix baseline tied to an exact commit.


## Source

| Measurement                 | Source of truth                     |
| --------------------------- | ----------------------------------- |
| VUs and iterations          | k6                                  |
| End-to-end p50/p95/p99      | k6                                  |
| k6 threshold pass/fail      | k6                                  |
| Client-observed failures    | k6                                  |
| Test throughput             | k6                                  |
| Server-side latency         | Grafana                             |
| JVM and connection pool     | Grafana                             |
| JDBC duration/query pattern | Grafana traces                      |
| N+1 evidence                | JDBC child spans/query count        |
| Root-cause investigation    | Grafana traces and PostgreSQL       |
