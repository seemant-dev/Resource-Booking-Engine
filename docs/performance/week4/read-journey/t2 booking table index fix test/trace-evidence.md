# Booking-order index — Run 1 evidence

Run 1 started 2026-10-21 21:01 IST. The representative `GET /api/bookings/me` trace started at 21:03:11 IST: HTTP 200, 53.5 ms, 11 total spans. Its joined bookings/slots/resources JDBC span took 5.75 ms. As in the N+1 fix test, there are no 50 separate slot SELECTs; the 11 spans are not a JDBC-query count.

The trace shows the SQL issued by the application, **not** the index PostgreSQL chose. The [EXPLAIN plan](explain.md) confirms `idx_bookings_user_booked_at_id` was used and the Sort was removed. Run 1's client-side booking-history p95 was 41.29 ms with zero HTTP failures; see [the k6 result](run-1.md). Grafana panels use server-side time windows, so their percentile values need not match the k6 run summary. The RED error panel shows “No data”; the zero-failure claim comes from k6.

## Screenshots

- [RED dashboard](evidence/run-1-red-dashboard.png)
- [Request rate](evidence/run-1-request-rate.png)
- [p50 latency](evidence/run-1-p50-latency.png)
- [p95 latency](evidence/run-1-p95-latency.png)
- [p99 latency](evidence/run-1-p99-latency.png)
- [Booking-history trace and joined SQL span](evidence/run-1-bookings-trace-and-query.png)
