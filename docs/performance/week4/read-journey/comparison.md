# Read journey — measured comparison

Local k6, three runs per stage: 10 looping VUs, 30s ramp-up, 120s steady, 15s ramp-down. Each journey reads resources, resource slots and page 0 of the heavy user's booking history (50 records). Dataset: 100 resources, 500 users, 60,000 slots and 50,000 bookings; the test user owns 10,000 bookings. The same workload and dataset were used throughout.

```text
T0  Original page query + 50 separate slot SELECTs
     │ @EntityGraph fetches slot and resource with the booking page
     ▼
T1  One joined page query; still reads 10,000 bookings and scans 60,000 slots
     │ V4 index on (user_id, booked_at DESC, id DESC)
     ▼
T2  Same joined SQL; index supplies the first 50 bookings in order, without a Sort
```

## k6 results

Booking-history client-side p95 (ms; lower is better):

```text
T0  175.96  ██████████████████
T1   83.37  ████████
T2   36.54  ████
             ~10 ms per block; median of the three run-level p95 values
```

| Stage | Change | Booking p95: runs 1 / 2 / 3 (ms) | Median run p95 (ms) | Mean request rate (req/s) |
|---|---|---:|---:|---:|
| [T0](<t0 baseline-first-test/trace-evidence.md>) | Original fetch plan and index | 175.01 / 175.96 / 176.98 | 175.96 | 22.80 |
| [T1](<t1 N+1 fix test/trace-evidence.md>) | `@EntityGraph` on `slot` and `slot.resource` | 84.61 / 80.01 / 83.37 | 83.37 | 23.83 |
| [T2](<t2 booking table index fix test/trace-evidence.md>) | Add booking-order index; fetch graph unchanged | 41.29 / 36.54 / 33.57 | 36.54 | 24.58 |

All nine runs passed their checks with zero HTTP failures. The declared booking p95 target was < 500 ms; T0 also met it, despite the inefficient queries. Because VUs loop, faster journeys also raise request rate; this is not a fixed-arrival-rate capacity test.

The two other endpoints stayed in a similar absolute range, but were not identical:

| Endpoint: mean of three run-level p95 values (ms) | T0 | T1 | T2 |
|---|---:|---:|---:|
| `GET /api/resources` | 17.86 | 16.95 | 18.40 |
| `GET /api/resources/{id}/slots` | 16.87 | 16.95 | 18.87 |

With looping VUs, mean total request rate rose from 22.80 req/s at T0 to 24.58 req/s at T2 as the booking journey became faster. That extra load may have contributed to the small T2 p95 increases on the other two endpoints; local variation is also possible. Because arrival rate was not held constant, these results do not establish a regression caused by the booking index.

## Query-plan evidence

The [T0 trace](<t0 baseline-first-test/trace-evidence.md>) shows 50 separate slot SELECTs for a 50-booking page. T1's entity graph removed those calls. T1 and T2 then used the **same joined page SQL**, making their PostgreSQL plans directly comparable:

| Joined page query | [T1: before index](<t1 N+1 fix test/explain.md>) | [T2: after index](<t2 booking table index fix test/explain.md>) |
| Metric                                          | Before optimization       | After optimization                        |
|-------------------------------------------------|---------------------------|-------------------------------------------|
| Booking rows read                               | 10,000                    | 50                                        |
| Slot access                                     | Sequential scan of 60,000 | 50 primary-key lookups in one SQL query   |
| Booking sort                                    | Top-N sort                | None                                      |
| Shared buffer hits                              | 767                       | 159                                       |
| PostgreSQL execution time (one EXPLAIN sample)  | 175.387 ms                | 17.167 ms                                 |

PostgreSQL chose `idx_bookings_user_booked_at_id` in T2. The EXPLAIN times are single database measurements, not k6 HTTP latency or a three-run SQL benchmark. The [T0 bookings-only plan](<t0 baseline-first-test/explain.md>) has a different query shape, so its execution time is not compared directly with T1/T2.

**Conclusion:** The repeated slot queries were removed, and the new index eliminated the remaining sort/large scan for the joined booking page. Booking-history p95 fell at each stage across all three local runs. These are results for the documented portfolio workload, not a production SLO or a capacity claim.
