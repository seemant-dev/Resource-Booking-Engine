# Admin bookings query plans

POST /api/admin/bookings?page=0&size=20 was tested with no filter, user email, resource name, and both filters. Each SQL statement below reproduces the corresponding Hibernate page SELECT: the same selected columns, joins, predicates, ordering, and fetch limit. The only substitutions are bind values: page size 20, userEmail = %user1@mailinator.com%, and resourceName = %resource1%.

The API also issues a separate count query for pagination. The four EXPLAIN plans here cover the page SELECTs, so their execution times are not complete API response times.

| Filters | Page SELECT execution | Rows entering final sort | Buffer hits | Total elements |
| --- | ---: | ---: | ---: | ---: |
| None | 186.488 ms | 50,018 | 1,046 | 50,018 |
| User email | 46.218 ms | 10,018 | 30,273 | 10,018 |
| Resource name | 70.035 ms | 6,002 | 25,273 | 6,002 |
| Both | 32.778 ms | 1,202 | 36,283 | 1,202 |

## 1. No filter

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT b1_0.id, b1_0.booked_at, b1_0.cancelled_at, b1_0.slot_id,
       s1_0.id, s1_0.is_booked, s1_0.created_at, s1_0.resource_id,
       r1_0.id, r1_0.created_at, r1_0.description, r1_0.name,
       s1_0.slot_end, s1_0.slot_start, s1_0.version,
       b1_0.status, b1_0.user_id,
       u1_0.id, u1_0.created_at, u1_0.email, u1_0.name,
       u1_0.password_hash, u1_0.role
FROM bookings b1_0
JOIN slots s1_0 ON s1_0.id = b1_0.slot_id
JOIN resources r1_0 ON r1_0.id = s1_0.resource_id
JOIN users u1_0 ON u1_0.id = b1_0.user_id
ORDER BY b1_0.booked_at DESC, b1_0.id DESC
FETCH FIRST 20 ROWS ONLY;
```

```text
Limit  (cost=4643.21..4643.26 rows=20 width=271) (actual time=183.960..183.984 rows=20 loops=1)
  Buffers: shared hit=1046
  ->  Sort  (cost=4643.21..4768.48 rows=50107 width=271) (actual time=183.958..183.981 rows=20 loops=1)
        Sort Key: b1_0.booked_at DESC, b1_0.id DESC
        Sort Method: top-N heapsort  Memory: 45kB
        Buffers: shared hit=1046
        ->  Hash Join  (cost=1938.62..3309.88 rows=50107 width=271) (actual time=104.751..165.846 rows=50018 loops=1)
              Hash Cond: (b1_0.user_id = u1_0.id)
              Buffers: shared hit=1043
              ->  Hash Join  (cost=1915.25..3153.97 rows=50107 width=147) (actual time=99.609..150.187 rows=50018 loops=1)
                    Hash Cond: (s1_0.resource_id = r1_0.id)
                    Buffers: shared hit=1032
                    ->  Hash Join  (cost=1911.00..3012.61 rows=50107 width=95) (actual time=96.820..138.409 rows=50018 loops=1)
                          Hash Cond: (b1_0.slot_id = s1_0.id)
                          Buffers: shared hit=1030
                          ->  Seq Scan on bookings b1_0  (cost=0.00..970.07 rows=50107 width=50) (actual time=0.007..9.110 rows=50018 loops=1)
                                Buffers: shared hit=469
                          ->  Hash  (cost=1161.00..1161.00 rows=60000 width=45) (actual time=88.880..88.881 rows=60024 loops=1)
                                Buckets: 65536  Batches: 1  Memory Usage: 5436kB
                                Buffers: shared hit=561
                                ->  Seq Scan on slots s1_0  (cost=0.00..1161.00 rows=60000 width=45) (actual time=0.075..41.050 rows=60024 loops=1)
                                      Buffers: shared hit=561
                    ->  Hash  (cost=3.00..3.00 rows=100 width=52) (actual time=1.106..1.107 rows=117 loops=1)
                          Buckets: 1024  Batches: 1  Memory Usage: 19kB
                          Buffers: shared hit=2
                          ->  Seq Scan on resources r1_0  (cost=0.00..3.00 rows=100 width=52) (actual time=0.011..0.129 rows=117 loops=1)
                                Buffers: shared hit=2
              ->  Hash  (cost=16.50..16.50 rows=550 width=124) (actual time=4.691..4.709 rows=530 loops=1)
                    Buckets: 1024  Batches: 1  Memory Usage: 90kB
                    Buffers: shared hit=11
                    ->  Seq Scan on users u1_0  (cost=0.00..16.50 rows=550 width=124) (actual time=0.085..0.600 rows=530 loops=1)
                          Buffers: shared hit=11
Planning:
  Buffers: shared hit=66
Planning Time: 24.662 ms
Execution Time: 186.488 ms
```

## 2. User email only

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT b1_0.id, b1_0.booked_at, b1_0.cancelled_at, b1_0.slot_id,
       s1_0.id, s1_0.is_booked, s1_0.created_at, s1_0.resource_id,
       r1_0.id, r1_0.created_at, r1_0.description, r1_0.name,
       s1_0.slot_end, s1_0.slot_start, s1_0.version,
       b1_0.status, b1_0.user_id,
       u2_0.id, u2_0.created_at, u2_0.email, u2_0.name,
       u2_0.password_hash, u2_0.role
FROM bookings b1_0
JOIN users u1_0 ON u1_0.id = b1_0.user_id
JOIN slots s1_0 ON s1_0.id = b1_0.slot_id
JOIN resources r1_0 ON r1_0.id = s1_0.resource_id
JOIN users u2_0 ON u2_0.id = b1_0.user_id
WHERE upper(u1_0.email) LIKE upper('%user1@mailinator.com%') ESCAPE '\'
ORDER BY b1_0.booked_at DESC, b1_0.id DESC
FETCH FIRST 20 ROWS ONLY;
```

```text
Limit  (cost=74.86..74.91 rows=20 width=271) (actual time=42.821..42.828 rows=20 loops=1)
  Buffers: shared hit=30273
  ->  Sort  (cost=74.86..75.09 rows=91 width=271) (actual time=42.818..42.823 rows=20 loops=1)
        Sort Key: b1_0.booked_at DESC, b1_0.id DESC
        Sort Method: top-N heapsort  Memory: 45kB
        Buffers: shared hit=30273
        ->  Hash Join  (cost=5.11..72.44 rows=91 width=271) (actual time=1.490..35.232 rows=10018 loops=1)
              Hash Cond: (s1_0.resource_id = r1_0.id)
              Buffers: shared hit=30273
              ->  Nested Loop  (cost=0.85..67.94 rows=91 width=219) (actual time=0.906..31.740 rows=10018 loops=1)
                    Buffers: shared hit=30271
                    ->  Nested Loop  (cost=0.56..34.68 rows=91 width=174) (actual time=0.859..6.725 rows=10018 loops=1)
                          Join Filter: (u1_0.id = b1_0.user_id)
                          Buffers: shared hit=217
                          ->  Nested Loop  (cost=0.28..27.57 rows=1 width=132) (actual time=0.705..0.940 rows=1 loops=1)
                                Buffers: shared hit=14
                                ->  Seq Scan on users u1_0  (cost=0.00..19.25 rows=1 width=8) (actual time=0.111..0.344 rows=1 loops=1)
                                      Filter: (upper((email)::text) ~~ '%USER1@MAILINATOR.COM%'::text)
                                      Rows Removed by Filter: 529
                                      Buffers: shared hit=11
                                ->  Index Scan using users_pkey on users u2_0  (cost=0.28..8.29 rows=1 width=124) (actual time=0.552..0.552 rows=1 loops=1)
                                      Index Cond: (id = u1_0.id)
                                      Buffers: shared hit=3
                          ->  Index Scan using idx_bookings_user on bookings b1_0  (cost=0.29..5.86 rows=100 width=50) (actual time=0.148..3.824 rows=10018 loops=1)
                                Index Cond: (user_id = u2_0.id)
                                Buffers: shared hit=203
                    ->  Index Scan using slots_pkey on slots s1_0  (cost=0.29..0.37 rows=1 width=45) (actual time=0.002..0.002 rows=1 loops=10018)
                          Index Cond: (id = b1_0.slot_id)
                          Buffers: shared hit=30054
              ->  Hash  (cost=3.00..3.00 rows=100 width=52) (actual time=0.165..0.166 rows=117 loops=1)
                    Buckets: 1024  Batches: 1  Memory Usage: 19kB
                    Buffers: shared hit=2
                    ->  Seq Scan on resources r1_0  (cost=0.00..3.00 rows=100 width=52) (actual time=0.017..0.093 rows=117 loops=1)
Planning:
  Buffers: shared hit=47
Planning Time: 8.431 ms
Execution Time: 46.218 ms
```

## 3. Resource name only

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT b1_0.id, b1_0.booked_at, b1_0.cancelled_at, b1_0.slot_id,
       s2_0.id, s2_0.is_booked, s2_0.created_at, s2_0.resource_id,
       r2_0.id, r2_0.created_at, r2_0.description, r2_0.name,
       s2_0.slot_end, s2_0.slot_start, s2_0.version,
       b1_0.status, b1_0.user_id,
       u1_0.id, u1_0.created_at, u1_0.email, u1_0.name,
       u1_0.password_hash, u1_0.role
FROM bookings b1_0
JOIN slots s1_0 ON s1_0.id = b1_0.slot_id
JOIN resources r1_0 ON r1_0.id = s1_0.resource_id
JOIN slots s2_0 ON s2_0.id = b1_0.slot_id
JOIN resources r2_0 ON r2_0.id = s2_0.resource_id
JOIN users u1_0 ON u1_0.id = b1_0.user_id
WHERE upper(r1_0.name) LIKE upper('%resource1%') ESCAPE '\'
ORDER BY b1_0.booked_at DESC, b1_0.id DESC
FETCH FIRST 20 ROWS ONLY;
```

```text
Limit  (cost=2015.26..2015.31 rows=20 width=271) (actual time=69.572..69.579 rows=20 loops=1)
  Buffers: shared hit=25273
  ->  Sort  (cost=2015.26..2016.52 rows=501 width=271) (actual time=69.570..69.576 rows=20 loops=1)
        Sort Key: b1_0.booked_at DESC, b1_0.id DESC
        Sort Method: top-N heapsort  Memory: 45kB
        Buffers: shared hit=25273
        ->  Hash Join  (cost=658.20..2001.93 rows=501 width=271) (actual time=22.395..67.010 rows=6002 loops=1)
              Hash Cond: (b1_0.user_id = u1_0.id)
              Buffers: shared hit=25273
              ->  Hash Join  (cost=634.83..1977.23 rows=501 width=147) (actual time=20.772..63.712 rows=6002 loops=1)
                    Hash Cond: (s2_0.resource_id = r2_0.id)
                    Buffers: shared hit=25262
                    ->  Nested Loop  (cost=630.58..1971.61 rows=501 width=95) (actual time=19.813..61.410 rows=6002 loops=1)
                          Buffers: shared hit=25260
                          ->  Hash Join  (cost=630.29..1793.27 rows=501 width=58) (actual time=19.802..38.581 rows=6002 loops=1)
                                Hash Cond: (b1_0.slot_id = s1_0.id)
                                Buffers: shared hit=7254
                                ->  Seq Scan on bookings b1_0  (cost=0.00..970.07 rows=50107 width=50) (actual time=0.004..6.158 rows=50018 loops=1)
                                      Buffers: shared hit=469
                                ->  Hash  (cost=622.79..622.79 rows=600 width=8) (actual time=19.134..19.136 rows=7212 loops=1)
                                      Buckets: 8192 (originally 1024)  Batches: 1 (originally 1)  Memory Usage: 346kB
                                      Buffers: shared hit=6785
                                      ->  Nested Loop  (cost=21.06..622.79 rows=600 width=8) (actual time=1.986..18.206 rows=7212 loops=1)
                                            Buffers: shared hit=6785
                                            ->  Seq Scan on resources r1_0  (cost=0.00..3.50 rows=1 width=8) (actual time=0.033..0.249 rows=12 loops=1)
                                                  Filter: (upper((name)::text) ~~ '%RESOURCE1%'::text)
                                                  Rows Removed by Filter: 105
                                                  Buffers: shared hit=2
                                            ->  Bitmap Heap Scan on slots s1_0  (cost=21.06..613.29 rows=600 width=16) (actual time=0.516..1.438 rows=601 loops=12)
                                                  Recheck Cond: (resource_id = r1_0.id)
                                                  Heap Blocks: exact=6722
                                                  Buffers: shared hit=6783
                                                  ->  Bitmap Index Scan on uk_slots_resource_start  (cost=0.00..20.91 rows=600 width=0) (actual time=0.459..0.459 rows=601 loops=12)
                                                        Index Cond: (resource_id = r1_0.id)
                                                        Buffers: shared hit=61
                          ->  Index Scan using slots_pkey on slots s2_0  (cost=0.29..0.36 rows=1 width=45) (actual time=0.003..0.003 rows=1 loops=6002)
                                Index Cond: (id = s1_0.id)
                                Buffers: shared hit=18006
                    ->  Hash  (cost=3.00..3.00 rows=100 width=52) (actual time=0.220..0.220 rows=117 loops=1)
                          Buckets: 1024  Batches: 1  Memory Usage: 19kB
                          Buffers: shared hit=2
                          ->  Seq Scan on resources r2_0  (cost=0.00..3.00 rows=100 width=52) (actual time=0.062..0.099 rows=117 loops=1)
              ->  Hash  (cost=16.50..16.50 rows=550 width=124) (actual time=0.748..0.749 rows=530 loops=1)
                    Buckets: 1024  Batches: 1  Memory Usage: 90kB
                    Buffers: shared hit=11
                    ->  Seq Scan on users u1_0  (cost=0.00..16.50 rows=550 width=124) (actual time=0.017..0.442 rows=530 loops=1)
                          Buffers: shared hit=11
Planning:
  Buffers: shared hit=42
Planning Time: 7.028 ms
Execution Time: 70.035 ms
```

## 4. User email and resource name

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT b1_0.id, b1_0.booked_at, b1_0.cancelled_at, b1_0.slot_id,
       s2_0.id, s2_0.is_booked, s2_0.created_at, s2_0.resource_id,
       r2_0.id, r2_0.created_at, r2_0.description, r2_0.name,
       s2_0.slot_end, s2_0.slot_start, s2_0.version,
       b1_0.status, b1_0.user_id,
       u2_0.id, u2_0.created_at, u2_0.email, u2_0.name,
       u2_0.password_hash, u2_0.role
FROM bookings b1_0
JOIN users u1_0 ON u1_0.id = b1_0.user_id
JOIN slots s1_0 ON s1_0.id = b1_0.slot_id
JOIN resources r1_0 ON r1_0.id = s1_0.resource_id
JOIN slots s2_0 ON s2_0.id = b1_0.slot_id
JOIN resources r2_0 ON r2_0.id = s2_0.resource_id
JOIN users u2_0 ON u2_0.id = b1_0.user_id
WHERE upper(u1_0.email) LIKE upper('%user1@mailinator.com%') ESCAPE '\'
  AND upper(r1_0.name) LIKE upper('%resource1%') ESCAPE '\'
ORDER BY b1_0.booked_at DESC, b1_0.id DESC
FETCH FIRST 20 ROWS ONLY;
```

```text
Limit  (cost=72.23..72.24 rows=1 width=271) (actual time=31.326..31.332 rows=20 loops=1)
  Buffers: shared hit=36283
  ->  Sort  (cost=72.23..72.24 rows=1 width=271) (actual time=31.324..31.329 rows=20 loops=1)
        Sort Key: b1_0.booked_at DESC, b1_0.id DESC
        Sort Method: top-N heapsort  Memory: 45kB
        Buffers: shared hit=36283
        ->  Nested Loop  (cost=4.80..72.22 rows=1 width=271) (actual time=0.563..29.753 rows=1202 loops=1)
              Buffers: shared hit=36283
              ->  Nested Loop  (cost=4.66..72.06 rows=1 width=219) (actual time=0.505..28.752 rows=1202 loops=1)
                    Buffers: shared hit=33879
                    ->  Hash Join  (cost=4.37..71.70 rows=1 width=182) (actual time=0.471..27.387 rows=1202 loops=1)
                          Hash Cond: (s1_0.resource_id = r1_0.id)
                          Buffers: shared hit=30273
                          ->  Nested Loop  (cost=0.85..67.94 rows=91 width=190) (actual time=0.275..26.447 rows=10018 loops=1)
                                Buffers: shared hit=30271
                                ->  Nested Loop  (cost=0.56..34.68 rows=91 width=174) (actual time=0.248..5.547 rows=10018 loops=1)
                                      Join Filter: (u1_0.id = b1_0.user_id)
                                      Buffers: shared hit=217
                                      ->  Nested Loop  (cost=0.28..27.57 rows=1 width=132) (actual time=0.129..0.418 rows=1 loops=1)
                                            Buffers: shared hit=14
                                            ->  Seq Scan on users u1_0  (cost=0.00..19.25 rows=1 width=8) (actual time=0.043..0.331 rows=1 loops=1)
                                                  Filter: (upper((email)::text) ~~ '%USER1@MAILINATOR.COM%'::text)
                                                  Rows Removed by Filter: 529
                                                  Buffers: shared hit=11
                                            ->  Index Scan using users_pkey on users u2_0  (cost=0.28..8.29 rows=1 width=124) (actual time=0.033..0.033 rows=1 loops=1)
                                                  Index Cond: (id = u1_0.id)
                                                  Buffers: shared hit=3
                                      ->  Index Scan using idx_bookings_user on bookings b1_0  (cost=0.29..5.86 rows=100 width=50) (actual time=0.117..3.403 rows=10018 loops=1)
                                            Index Cond: (user_id = u2_0.id)
                                            Buffers: shared hit=203
                                ->  Index Scan using slots_pkey on slots s1_0  (cost=0.29..0.37 rows=1 width=16) (actual time=0.002..0.002 rows=1 loops=10018)
                                      Index Cond: (id = b1_0.slot_id)
                                      Buffers: shared hit=30054
                          ->  Hash  (cost=3.50..3.50 rows=1 width=8) (actual time=0.153..0.153 rows=12 loops=1)
                                Buckets: 1024  Batches: 1  Memory Usage: 9kB
                                Buffers: shared hit=2
                                ->  Seq Scan on resources r1_0  (cost=0.00..3.50 rows=1 width=8) (actual time=0.060..0.112 rows=12 loops=1)
                                      Filter: (upper((name)::text) ~~ '%RESOURCE1%'::text)
                                      Rows Removed by Filter: 105
                                      Buffers: shared hit=2
                    ->  Index Scan using slots_pkey on slots s2_0  (cost=0.29..0.36 rows=1 width=45) (actual time=0.001..0.001 rows=1 loops=1202)
                          Index Cond: (id = s1_0.id)
                          Buffers: shared hit=3606
              ->  Index Scan using resources_pkey on resources r2_0  (cost=0.14..0.16 rows=1 width=52) (actual time=0.000..0.000 rows=1 loops=1202)
                    Index Cond: (id = s2_0.resource_id)
                    Buffers: shared hit=2404
Planning:
  Buffers: shared hit=68
Planning Time: 5.983 ms
Execution Time: 32.778 ms
```

## Count validation

The separate count diagnostic returned:
| Filter | Expected `totalElements` |
| --- | ---: |
| No filter | 50,018 |
| User email only | 10,018 |
| Resource name only | 6,002 |
| Both filters | 1,202 |

for the four filter combinations above. These are the expected totalElements values if the API reads the same database snapshot. The resource filter is substring matching: resource1 also matches names such as resource10 through resource19 and resource100.

## Performance

| Filter | Page SELECT execution time | Observation |
| --- | ---: | --- |
| No filter | 186.488 ms | Joins 50,018 bookings before returning 20. |
| User email only | 46.218 ms | Fetches 10,018 matching bookings before sorting. |
| Resource name only | 70.035 ms | Fetches 6,002 matching bookings with repeated slot/resource joins. |
| Both filters | 32.778 ms | Fetches 1,202 matching bookings with repeated joins. |

## Findings

- All four page queries sort every matching joined row before returning 20. The no-filter query joins all 50,018 bookings and 60,024 slots before its top-N sort.
- The filtered plans include duplicate joins for relationship filtering and entity-graph fetching (u1/u2 and s1/s2/r1/r2). They remain single SQL requests, but produce many buffer accesses; this is not an N+1 sequence.
- The email plan uses idx_bookings_user after finding the user. The resource plan uses uk_slots_resource_start by resource_id. The current indexes therefore help parts of the filtered queries, while neither removes all work before sorting.
- The planner underestimates filtered result sizes (for example, one estimated row versus 1,202 actual rows in the combined plan). These plans are evidence for further measurement, not a before/after improvement claim.
