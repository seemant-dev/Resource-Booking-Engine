# Admin stats query plan

`GET /api/admin/stats` runs three aggregate counts in one SQL statement. Both plans below use the same query structure with time bounds from their respective runs.

## First plan — before adding near-future slots

The values reproduce the traced query for the 25 October 2026, 13:49 IST request. `EXPLAIN ANALYZE` was run against the database afterward, so its row counts reflect the database at that time.

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT
    (SELECT count(*)
     FROM resources) AS "totalResources",
    (SELECT count(*)
     FROM bookings
     WHERE booked_at >= TIMESTAMPTZ '2026-10-24 18:30:00+00'
       AND booked_at <  TIMESTAMPTZ '2026-10-25 08:19:34.530327+00') AS "bookingsToday",
    (SELECT count(*)
     FROM slots
     WHERE is_booked = false
       AND slot_start >= TIMESTAMPTZ '2026-10-25 08:19:34.530327+00'
       AND slot_start <  TIMESTAMPTZ '2026-10-26 08:19:34.530327+00') AS "slotsExpiringUnbooked";
```

```text
Result  (cost=2684.89..2684.90 rows=1 width=24) (actual time=76.079..76.093 rows=1 loops=1)
  Buffers: shared hit=1032
  InitPlan 1 (returns $0)
    ->  Aggregate  (cost=3.25..3.26 rows=1 width=8) (actual time=0.177..0.189 rows=1 loops=1)
          Buffers: shared hit=2
          ->  Seq Scan on resources  (cost=0.00..3.00 rows=100 width=0) (actual time=0.102..0.155 rows=117 loops=1)
                Buffers: shared hit=2
  InitPlan 2 (returns $1)
    ->  Aggregate  (cost=1220.61..1220.62 rows=1 width=8) (actual time=34.447..34.448 rows=1 loops=1)
          Buffers: shared hit=469
          ->  Seq Scan on bookings  (cost=0.00..1220.61 rows=1 width=0) (actual time=33.797..34.439 rows=10 loops=1)
                Filter: ((booked_at >= '2026-10-24 18:30:00+00'::timestamp with time zone) AND (booked_at < '2026-10-25 08:19:34.530327+00'::timestamp with time zone))
                Rows Removed by Filter: 50008
                Buffers: shared hit=469
  InitPlan 3 (returns $2)
    ->  Aggregate  (cost=1461.00..1461.01 rows=1 width=8) (actual time=41.434..41.435 rows=1 loops=1)
          Buffers: shared hit=561
          ->  Seq Scan on slots  (cost=0.00..1461.00 rows=1 width=0) (actual time=41.430..41.430 rows=0 loops=1)
                Filter: ((NOT is_booked) AND (slot_start >= '2026-10-25 08:19:34.530327+00'::timestamp with time zone) AND (slot_start < '2026-10-26 08:19:34.530327+00'::timestamp with time zone))
                Rows Removed by Filter: 60012
                Buffers: shared hit=561
Planning:
  Buffers: shared hit=122
Planning Time: 26.887 ms
Execution Time: 80.210 ms
```

### Observations

| Count | Rows inspected | Rows matched | Scan time | Buffer hits |
| --- | ---: | ---: | ---: | ---: |
| Resources | 117 | 117 | 0.155 ms | 2 |
| Bookings today | 50,018 | 10 | 34.439 ms | 469 |
| Unbooked slots expiring | 60,012 | 0 | 41.430 ms | 561 |

The resource scan is small. The other two counts scan their entire tables: the booking index starts with `user_id`, and the slot uniqueness index starts with `resource_id`, but these counts filter on neither column. The measured execution time is 80.210 ms with all 1,032 buffer accesses reported as shared hits. This is a single measurement, not an API latency baseline. As these tables grow, the two full scans can become more expensive; evaluate indexes on `bookings(booked_at)` and unbooked slots by `slot_start` against the actual admin workload before adding them.

An earlier API response reported 105 resources and 2 bookings today, while this plan scanned 117 resources and matched 10 bookings. These results were taken at different times and cannot be treated as the same database snapshot.

## Second plan — after adding 12 unbooked slots

Twelve unbooked slots were inserted with starts between one and twelve hours in the future. The same SQL was rerun with the current time bounds:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT
    (SELECT count(*)
     FROM resources) AS "totalResources",
    (SELECT count(*)
     FROM bookings
     WHERE booked_at >= TIMESTAMPTZ '2026-10-24 18:30:00+00'
       AND booked_at <  TIMESTAMPTZ '2026-10-25 08:53:28+00') AS "bookingsToday",
    (SELECT count(*)
     FROM slots
     WHERE is_booked = false
       AND slot_start >= TIMESTAMPTZ '2026-10-25 08:53:28+00'
       AND slot_start <  TIMESTAMPTZ '2026-10-26 08:53:28+00') AS "slotsExpiringUnbooked";
```

```text
Result  (cost=2684.89..2684.90 rows=1 width=24) (actual time=157.447..157.450 rows=1 loops=1)
  Buffers: shared hit=1032
  InitPlan 1 (returns $0)
    ->  Aggregate  (cost=3.25..3.26 rows=1 width=8) (actual time=0.061..0.062 rows=1 loops=1)
          Buffers: shared hit=2
          ->  Seq Scan on resources  (cost=0.00..3.00 rows=100 width=0) (actual time=0.025..0.051 rows=117 loops=1)
                Buffers: shared hit=2
  InitPlan 2 (returns $1)
    ->  Aggregate  (cost=1220.61..1220.62 rows=1 width=8) (actual time=119.174..119.175 rows=1 loops=1)
          Buffers: shared hit=469
          ->  Seq Scan on bookings  (cost=0.00..1220.61 rows=1 width=0) (actual time=118.716..119.167 rows=10 loops=1)
                Filter: ((booked_at >= '2026-10-24 18:30:00+00'::timestamp with time zone) AND (booked_at < '2026-10-25 08:53:28+00'::timestamp with time zone))
                Rows Removed by Filter: 50008
                Buffers: shared hit=469
  InitPlan 3 (returns $2)
    ->  Aggregate  (cost=1461.00..1461.01 rows=1 width=8) (actual time=38.203..38.203 rows=1 loops=1)
          Buffers: shared hit=561
          ->  Seq Scan on slots  (cost=0.00..1461.00 rows=1 width=0) (actual time=38.195..38.196 rows=12 loops=1)
                Filter: ((NOT is_booked) AND (slot_start >= '2026-10-25 08:53:28+00'::timestamp with time zone) AND (slot_start < '2026-10-26 08:53:28+00'::timestamp with time zone))
                Rows Removed by Filter: 60012
                Buffers: shared hit=561
Planning Time: 0.599 ms
Execution Time: 157.723 ms
```

### Comparison

| Metric | First plan | Second plan |
| --- | ---: | ---: |
| Bookings matched / scanned | 10 / 50,018 | 10 / 50,018 |
| Slots matched / scanned | 0 / 60,012 | 12 / 60,024 |
| Booking scan time | 34.439 ms | 119.167 ms |
| Slot scan time | 41.430 ms | 38.196 ms |
| Total execution time | 80.210 ms | 157.723 ms |

The plan shape did not change: both counts still use sequential scans. The new slots made the expiring count nonzero, while the slot scan time remained similar. Most of the total-time difference came from the bookings scan, which read the same number of rows; these two runs do not establish a performance effect from adding the slots.
