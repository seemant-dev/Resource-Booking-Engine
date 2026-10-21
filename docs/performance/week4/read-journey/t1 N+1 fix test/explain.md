# Booking page query plan — after N+1 fix, before index change

This is the joined page query shown in the JDBC span, with its two parameters set to user ID 1 and page size 50. The original `idx_bookings_user` index is unchanged.

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
Limit  (cost=2802.33..2802.45 rows=50 width=147) (actual time=170.440..170.909 rows=50 loops=1)
  Buffers: shared hit=767
  ->  Sort  (cost=2802.33..2827.34 rows=10005 width=147) (actual time=170.420..170.886 rows=50 loops=1)
        Sort Key: b1_0.booked_at DESC, b1_0.id DESC
        Sort Method: top-N heapsort  Memory: 50kB
        Buffers: shared hit=767
        ->  Hash Join  (cost=848.48..2469.97 rows=10005 width=147) (actual time=53.487..166.521 rows=10000 loops=1)
              Hash Cond: (s1_0.resource_id = r1_0.id)
              Buffers: shared hit=767
              ->  Nested Loop  (cost=844.23..2438.34 rows=10005 width=95) (actual time=52.519..163.084 rows=10000 loops=1)
                    Buffers: shared hit=765
                    ->  Index Only Scan using users_pkey on users u1_0  (cost=0.27..8.29 rows=1 width=8) (actual time=1.382..1.667 rows=1 loops=1)
                          Index Cond: (id = 1)
                          Heap Fetches: 1
                          Buffers: shared hit=3
                    ->  Hash Join  (cost=843.95..2330.00 rows=10005 width=95) (actual time=51.102..160.007 rows=10000 loops=1)
                          Hash Cond: (s1_0.id = b1_0.slot_id)
                          Buffers: shared hit=762
                          ->  Seq Scan on slots s1_0  (cost=0.00..1161.00 rows=60000 width=45) (actual time=0.055..49.705 rows=60000 loops=1)
                                Buffers: shared hit=561
                          ->  Hash  (cost=718.89..718.89 rows=10005 width=50) (actual time=48.161..48.164 rows=10000 loops=1)
                                Buckets: 16384  Batches: 1  Memory Usage: 910kB
                                Buffers: shared hit=201
                                ->  Bitmap Heap Scan on bookings b1_0  (cost=125.83..718.89 rows=10005 width=50) (actual time=6.719..29.828 rows=10000 loops=1)
                                      Recheck Cond: (user_id = 1)
                                      Heap Blocks: exact=192
                                      Buffers: shared hit=201
                                      ->  Bitmap Index Scan on idx_bookings_user  (cost=0.00..123.33 rows=10005 width=0) (actual time=6.148..6.149 rows=10000 loops=1)
                                            Index Cond: (user_id = 1)
                                            Buffers: shared hit=9
              ->  Hash  (cost=3.00..3.00 rows=100 width=52) (actual time=0.353..0.354 rows=100 loops=1)
                    Buckets: 1024  Batches: 1  Memory Usage: 17kB
                    Buffers: shared hit=2
                    ->  Seq Scan on resources r1_0  (cost=0.00..3.00 rows=100 width=52) (actual time=0.037..0.052 rows=100 loops=1)
                          Buffers: shared hit=2
Planning:
  Buffers: shared hit=7
Planning Time: 14.395 ms
Execution Time: 175.387 ms
```

The plan reads 10,000 bookings and scans 60,000 slots before sorting and returning 50 rows. It used 767 shared buffer hits and took 175.387 ms in this single PostgreSQL execution. This is **not** the k6 request latency. The query changed with the fetch fix, so this execution time is not a like-for-like comparison with the bookings-only plan from the first test. Use this same joined query for the later index comparison.
