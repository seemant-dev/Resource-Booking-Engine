# Read journey — version A, run 2

- Started: 2026-10-19 22:42 IST (reported start time)
- Commit: `9f18dd6251c4dd1f754475accf445f33a3fb33ac`
- Dataset: Same performance seed as Run 1 (100 resources, 500 users, 60,000 slots, 50,000 bookings; heavy user: 10,000 bookings)
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
    ✓ 'p(95)<500' p(95)=175.96ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=15.69ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=17.55ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 7604    45.714671/s
    checks_succeeded...: 100.00% 7604 out of 7604
    checks_failed......: 0.00%   0 out of 7604

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=44.42ms  min=2.61ms  med=12.2ms  p(90)=113.23ms p(95)=140.22ms p(99)=188.32ms max=1.24s   
      { expected_response:true }...: avg=44.42ms  min=2.61ms  med=12.2ms  p(90)=113.23ms p(95)=140.22ms p(99)=188.32ms max=1.24s   
      { name:my_bookings }.........: avg=111.72ms min=50.04ms med=102ms   p(90)=150.21ms p(95)=175.96ms p(99)=215.24ms max=393.8ms 
      { name:resource_slots }......: avg=9.62ms   min=3.78ms  med=8.84ms  p(90)=13.92ms  p(95)=15.69ms  p(99)=21.9ms   max=102.36ms
      { name:resources }...........: avg=10.99ms  min=2.61ms  med=10.12ms p(90)=15.6ms   p(95)=17.55ms  p(99)=29.51ms  max=427.1ms 
    http_req_failed................: 0.00%  0 out of 3802
      { name:my_bookings }.........: 0.00%  0 out of 1267
      { name:resource_slots }......: 0.00%  0 out of 1267
      { name:resources }...........: 0.00%  0 out of 1267
    http_reqs......................: 3802   22.857336/s

    EXECUTION
    iteration_duration.............: avg=1.13s    min=1.05s   med=1.12s   p(90)=1.17s    p(95)=1.2s     p(99)=1.25s    max=1.53s   
    iterations.....................: 1267   7.617108/s
    vus............................: 1      min=0         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 33 MB  199 kB/s
    data_sent......................: 774 kB 4.7 kB/s




running (2m46.3s), 00/10 VUs, 1267 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations

- 1,267 journeys and 3,802 HTTP requests. All 7,604 checks passed; HTTP failures: 0/3,802.
- Booking-history p95: 175.96 ms (Run 1: 175.01 ms), below the 500 ms target. Resources p95: 17.55 ms; slots p95: 15.69 ms.
- No separate JDBC trace or query plan was captured for this run. The N+1 finding is documented in [Run 1's trace](trace-evidence.md).

[RED dashboard screenshot](<evidence screenshots/run-2-red-dashboard.png>)
