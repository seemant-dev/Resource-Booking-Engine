# Version A — Run 1 evidence

Date: 2026-10-19 21:30:00 IST. The representative trace below started at 21:27:32.936 IST.

- [Run 1 result and SQL plan](run-1.md): 1,263 journeys, 3,790 requests, 7,580/7,580 checks passed, zero HTTP failures. Booking-history p95: 175.01 ms.
- Trace ID: `078d14ee2c52446e4640f1319160240f`.
- Trace route: `GET /api/bookings/me`; duration: 103.83 ms; 62 total spans.

## Observation

The booking-history trace contains 50 separate `SELECT ... FROM slots WHERE id=?` JDBC spans for a 50-record page. The 62 spans are the **entire trace**, not the JDBC-query count. The booking page's `EXPLAIN (ANALYZE, BUFFERS)` output is recorded in [Run 1](run-1.md).

Grafana shows server-side, time-windowed metrics. The k6 result above is the client-side benchmark measurement. 
The RED dashboard's error panel says “No data”; zero failures are supported by k6's 0/3,790 result.

## Screenshots

- [RED dashboard](<evidence screenshots/run-1-red-dashboard.png>)
- [Request rate](<evidence screenshots/run-1-request-rate.png>)
- [p50 latency](<evidence screenshots/run-1-p50-latency.png>)
- [p95 latency](<evidence screenshots/run-1-p95-latency.png>)
- [p99 latency](<evidence screenshots/run-1-p99-latency.png>)
- [Booking-history trace overview](<evidence screenshots/run-1-bookings-trace-overview.png>)
- [Repeated slot-query span detail](<evidence screenshots/run-1-slot-jdbc-span-detail.png>)
