# Read journey — version A, run 1

```text
Journey: Read journey
Run: 1 of 3
Timestamp: 2026-10-19 21:30:00 IST (rough timestamp)
Commit: 9f18dd6251c4dd1f754475accf445f33a3fb33ac
Dataset: 100 resources, 500 users, 60,000 slots, 50,000 bookings; heavy user has 10,000 bookings
Command: & "C:\tools\k6\k6-v2.3.0-windows-amd64\k6.exe" run .\k6\read-journey.js

--- k6 output below ---
execution: local
        script: .\k6\read-journey.js
        output: -

     scenarios: (100.00%) 1 scenario, 10 max VUs, 3m15s max duration (incl. graceful stop):
              * default: Up to 10 looping VUs for 2m45s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)



  █ THRESHOLDS 

    checks
    ✓ 'rate==1' rate=100.00%

    http_req_duration{name:my_bookings}
    ✓ 'p(95)<500' p(95)=175.01ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=17.63ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=20.6ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 7580    45.445096/s
    checks_succeeded...: 100.00% 7580 out of 7580
    checks_failed......: 0.00%   0 out of 7580

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=46.05ms  min=3.16ms  med=13.61ms  p(90)=115.42ms p(95)=136.32ms p(99)=196.77ms max=996.62ms
      { expected_response:true }...: avg=46.05ms  min=3.16ms  med=13.61ms  p(90)=115.42ms p(95)=136.32ms p(99)=196.77ms max=996.62ms
      { name:my_bookings }.........: avg=112.16ms min=60.61ms med=103.36ms p(90)=148.95ms p(95)=175.01ms p(99)=211.4ms  max=648.21ms
      { name:resource_slots }......: avg=11.84ms  min=4.19ms  med=9.72ms   p(90)=14.98ms  p(95)=17.63ms  p(99)=34.21ms  max=377.59ms
      { name:resources }...........: avg=13.39ms  min=3.16ms  med=11.41ms  p(90)=17.05ms  p(95)=20.6ms   p(99)=37.51ms  max=370.58ms
    http_req_failed................: 0.00%  0 out of 3790
      { name:my_bookings }.........: 0.00%  0 out of 1263
      { name:resource_slots }......: 0.00%  0 out of 1263
      { name:resources }...........: 0.00%  0 out of 1263
    http_reqs......................: 3790   22.722548/s

    EXECUTION
    iteration_duration.............: avg=1.14s    min=1.07s   med=1.12s    p(90)=1.18s    p(95)=1.2s     p(99)=1.46s    max=1.67s   
    iterations.....................: 1263   7.572184/s
    vus............................: 1      min=0         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 33 MB  198 kB/s
    data_sent......................: 772 kB 4.6 kB/s




running (2m46.8s), 00/10 VUs, 1263 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations
- The local 10-VU run completed 1,263 journeys and 3,790 HTTP requests. All 7,580 checks passed; HTTP failures were 0/3,790. The booking-history route measured 175.01 ms p95 and 211.4 ms p99 in k6.
- In [the representative booking trace](trace-evidence.md) (ID `078d14ee2c52446e4640f1319160240f`), a 50-booking page triggered **50 separate slot SELECTs**. One resource SELECT was also observed; the page's bookings share a resource. The trace contains 62 spans in total; that is not the JDBC-query count.
- The page query used `idx_bookings_user` to find all 10,000 bookings for user 1, then sorted them to return 50. The sort used 32 kB of memory. PostgreSQL reported 30.791 ms execution time and 210 shared buffer hits. These are SQL measurements, not the end-to-end k6 latency.
- Run 1 met the p95 target. Trace inspection showed **50 individual slot SELECTs for a 50-booking page, indicating an N+1 query issue** despite acceptable response time.
