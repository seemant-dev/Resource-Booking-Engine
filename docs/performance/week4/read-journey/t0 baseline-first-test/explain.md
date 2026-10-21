# Booking page query plan — before N+1 fix

The page query used `idx_bookings_user` to find 10,000 bookings for user 1, then sorted them to return 50. PostgreSQL reported 30.791 ms execution time and 210 shared buffer hits. This plan covers the bookings query only; the separate slot/resource queries are visible in [the trace](trace-evidence.md).

Run against the seeded PostgreSQL database with `user1@mailinator.com` confirmed as user ID 1:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT b.id, b.slot_id, b.user_id, b.status, b.booked_at, b.cancelled_at
FROM bookings b
WHERE b.user_id = 1
ORDER BY b.booked_at DESC, b.id DESC
LIMIT 50 OFFSET 0;
```

```text
Limit  (cost=1047.67..1047.79 rows=50 width=50) (actual time=30.648..30.657 rows=50 loops=1)
  Buffers: shared hit=210
  ->  Sort  (cost=1047.67..1072.51 rows=9938 width=50) (actual time=30.646..30.650 rows=50 loops=1)
        Sort Key: booked_at DESC, id DESC
        Sort Method: top-N heapsort  Memory: 32kB
        Buffers: shared hit=210
        ->  Bitmap Heap Scan on bookings b  (cost=125.31..717.53 rows=9938 width=50) (actual time=2.986..27.253 rows=10000 loops=1)
              Recheck Cond: (user_id = 1)
              Heap Blocks: exact=192
              Buffers: shared hit=204
              ->  Bitmap Index Scan on idx_bookings_user  (cost=0.00..122.83 rows=9938 width=0) (actual time=2.099..2.100 rows=10000 loops=1)
                    Index Cond: (user_id = 1)
                    Buffers: shared hit=12
Planning:
  Buffers: shared hit=26
Planning Time: 3.329 ms
Execution Time: 30.791 ms
```
