# Booking page query plan — with booking-order index

Same joined page query used in the [N+1 fix test](<../N+1 fix test/explain.md>), with user ID 1 and page size 50. V4 adds `idx_bookings_user_booked_at_id`.

```sql
EXPLAIN (ANALYZE, BUFFERS)
select b1_0.id,b1_0.booked_at,b1_0.cancelled_at,b1_0.slot_id,s1_0.id,s1_0.is_booked,s1_0.created_at,s1_0.resource_id,r1_0.id,r1_0.created_at,r1_0.description,r1_0.name,s1_0.slot_end,s1_0.slot_start,s1_0.version,b1_0.status,b1_0.user_id
from bookings b1_0
join users u1_0 on u1_0.id=b1_0.user_id
join slots s1_0 on s1_0.id=b1_0.slot_id
join resources r1_0 on r1_0.id=s1_0.resource_id
where u1_0.id=1
order by b1_0.booked_at desc,b1_0.id desc
fetch first 50 rows only;
```

```text
Limit  (cost=1.13..44.83 rows=50 width=147) (actual time=7.018..16.527 rows=50 loops=1)
  Buffers: shared hit=159
  ->  Nested Loop  (cost=1.13..8708.58 rows=9962 width=147) (actual time=7.017..16.517 rows=50 loops=1)
        Buffers: shared hit=159
        ->  Nested Loop  (cost=0.98..8444.84 rows=9962 width=95) (actual time=6.507..15.910 rows=50 loops=1)
              Buffers: shared hit=157
              ->  Nested Loop  (cost=0.69..2473.52 rows=9962 width=50) (actual time=5.801..5.897 rows=50 loops=1)
                    Buffers: shared hit=7
                    ->  Index Scan using idx_bookings_user_booked_at_id on bookings b1_0  (cost=0.41..2340.71 rows=9962 width=50) (actual time=4.036..4.081 rows=50 loops=1)
                          Index Cond: (user_id = 1)
                          Buffers: shared hit=4
                    ->  Materialize  (cost=0.27..8.30 rows=1 width=8) (actual time=0.035..0.035 rows=1 loops=50)
                          Buffers: shared hit=3
                          ->  Index Only Scan using users_pkey on users u1_0  (cost=0.27..8.29 rows=1 width=8) (actual time=1.727..1.728 rows=1 loops=1)
                                Index Cond: (id = 1)
                                Heap Fetches: 1
                                Buffers: shared hit=3
              ->  Index Scan using slots_pkey on slots s1_0  (cost=0.29..0.60 rows=1 width=45) (actual time=0.194..0.194 rows=1 loops=50)
                    Index Cond: (id = b1_0.slot_id)
                    Buffers: shared hit=150
        ->  Memoize  (cost=0.15..0.17 rows=1 width=52) (actual time=0.011..0.011 rows=1 loops=50)
              Cache Key: s1_0.resource_id
              Cache Mode: logical
              Hits: 49  Misses: 1  Evictions: 0  Overflows: 0  Memory Usage: 1kB
              Buffers: shared hit=2
              ->  Index Scan using resources_pkey on resources r1_0  (cost=0.14..0.16 rows=1 width=52) (actual time=0.474..0.474 rows=1 loops=1)
                    Index Cond: (id = s1_0.resource_id)
                    Buffers: shared hit=2
Planning:
  Buffers: shared hit=177
Planning Time: 25.699 ms
Execution Time: 17.167 ms
```

PostgreSQL used the new index, returned the first 50 bookings without a Sort, and made 50 primary-key slot lookups inside this one SQL statement. Unlike the pre-index plan, it did not scan all 60,000 slots before applying the limit. Shared buffer hits were 159 versus 767 in the pre-index plan. Execution times (17.167 ms versus 175.387 ms) are single EXPLAIN samples, not repeated latency measurements.
