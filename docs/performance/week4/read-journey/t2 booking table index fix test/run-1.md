# Read journey — index test, run 1

- Started: 2026-10-21 21:01 IST
- Commit: `f4b7a1e26c6e197d86693e2889e86a74b0b1deec`
- Change from N+1 fix test: V4 booking-order index; `@EntityGraph` and workload unchanged
- Dataset: 100 resources, 500 users, 60,000 slots, 50,000 bookings; heavy user: 10,000 bookings
- Command: `& "C:\tools\k6\k6-v2.3.0-windows-amd64\k6.exe" run .\k6\read-journey.js`

## k6 output

```text
     execution: local
        script: .\k6\read-journey.js
        output: -

     scenarios: (100.00%) 1 scenario, 10 max VUs, 3m15s max duration (incl. graceful stop):
              * default: Up to 10 looping VUs for 2m45s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)



  █ THRESHOLDS 

    checks
    ✓ 'rate==1' rate=100.00%

    http_req_duration{name:my_bookings}
    ✓ 'p(95)<500' p(95)=41.29ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=19.45ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=19.55ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 8162    48.936863/s
    checks_succeeded...: 100.00% 8162 out of 8162
    checks_failed......: 0.00%   0 out of 8162

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=18.65ms min=4.85ms  med=15.28ms p(90)=31.13ms p(95)=35.67ms p(99)=46.15ms max=881.59ms
      { expected_response:true }...: avg=18.65ms min=4.85ms  med=15.28ms p(90)=31.13ms p(95)=35.67ms p(99)=46.15ms max=881.59ms
      { name:my_bookings }.........: avg=28.56ms min=14.04ms med=26.83ms p(90)=37.87ms p(95)=41.29ms p(99)=54.3ms  max=192.2ms 
      { name:resource_slots }......: avg=14.21ms min=6.57ms  med=14.23ms p(90)=17.91ms p(95)=19.45ms p(99)=25.54ms max=81.23ms 
      { name:resources }...........: avg=12.54ms min=4.85ms  med=12.31ms p(90)=16.51ms p(95)=19.55ms p(99)=29.04ms max=42.86ms 
    http_req_failed................: 0.00%  0 out of 4081
      { name:my_bookings }.........: 0.00%  0 out of 1360
      { name:resource_slots }......: 0.00%  0 out of 1360
      { name:resources }...........: 0.00%  0 out of 1360
    http_reqs......................: 4081   24.468431/s

    EXECUTION
    iteration_duration.............: avg=1.05s   min=1.02s   med=1.05s   p(90)=1.07s   p(95)=1.08s   p(99)=1.1s    max=1.32s   
    iterations.....................: 1360   8.154145/s
    vus............................: 1      min=1         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 36 MB  213 kB/s
    data_sent......................: 831 kB 5.0 kB/s




running (2m46.8s), 00/10 VUs, 1360 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations

- 1,360 journeys and 4,081 HTTP requests completed. All 8,162 checks passed; HTTP failures: 0/4,081.
- Booking-history p95 was 41.29 ms, versus 80.01–84.61 ms across the three N+1-fix runs. This is the first recorded index run; two more runs remain before a repeatability claim.
- The [same joined query's plan](explain.md) now uses `idx_bookings_user_booked_at_id` to read 50 bookings in order, with no Sort. PostgreSQL reported 159 shared buffer hits and 17.167 ms execution time for that single plan run. These are not the end-to-end k6 latency.
- [Grafana evidence](trace-evidence.md) shows the booking request and its joined JDBC query. Grafana's time-windowed latency panels are supporting server-side context; the k6 summary above is the client-side result.
