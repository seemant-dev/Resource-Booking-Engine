# Booking contention comparison

Three local HTTP rounds per strategy used the same [k6 workload](../../../../k6/booking-contention.js): 20 VUs each made one booking attempt against a fresh available slot. Login ran once per round. Run numbers are within each strategy, not time-matched pairs.

| Strategy | Conflict response p95: run 1 / 2 / 3 | Median run p95 |
|---|---:|---:|
| Optimistic | [410.50](optimistic-run.md) / [236.45](optimistic-run2.md) / [91.57](optimistic-run3.md) ms | 236.45 ms |
| Pessimistic | [292.78](pessimistic-run.md) / [184.49](pessimistic-run2.md) / [130.59](pessimistic-run3.md) ms | 184.49 ms |

All six rounds returned **1 `201`, 19 `409`, and zero unexpected responses**. A database check confirmed exactly one booking for each target slot. Pessimistic had the lower median run-level conflict p95, but optimistic was faster in its third round; these short local bursts do not establish a general winner.
