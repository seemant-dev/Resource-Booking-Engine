# N+1 fix — trace evidence

Representative Run 3 trace: `GET /api/bookings/me`, 2026-10-21 01:22:29 IST, trace ID `15812ab49f051a649f5a5da84c6d9d15`. The request took 86.13 ms and the trace contains 11 spans in total.

The booking page is now fetched with a join across bookings, slots and resources; a separate count query supplies pagination totals. The joined JDBC span took 73.34 ms in this trace. The 50 separate slot SELECTs seen in the [first test](../baseline-first-test/trace-evidence.md) are absent here. The count of 11 includes non-JDBC spans; it is not a query count.

Across the three unchanged k6 runs, booking-history p95 was 84.61, 80.01 and 83.37 ms, versus 175.01–176.98 ms in the first test. All checks passed with no HTTP failures. The [SQL plan](explain.md) still reads and joins 10,000 bookings before returning 50, leaving the ordering index as a separate experiment.

## Screenshots

- [Run 3 RED dashboard](evidence/run-3-red-dashboard.png)
- [Request rate](evidence/run-3-request-rate.png)
- [p50 latency](evidence/run-3-p50-latency.png)
- [p95 latency](evidence/run-3-p95-latency.png)
- [p99 latency](evidence/run-3-p99-latency.png)
- [Booking-history trace overview](evidence/run-3-bookings-trace-overview.png)
- [Joined booking query span](evidence/run-3-bookings-joined-query-span.png)
