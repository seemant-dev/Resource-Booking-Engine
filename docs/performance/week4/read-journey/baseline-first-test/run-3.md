# Read journey — version A, run 3

- Started: 2026-10-19 23:09 IST (reported start time)
- Commit: `9f18dd6251c4dd1f754475accf445f33a3fb33ac`
- Dataset: Same performance seed as Runs 1 and 2 (100 resources, 500 users, 60,000 slots, 50,000 bookings; heavy user: 10,000 bookings)
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
    ✓ 'p(95)<500' p(95)=176.98ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=17.3ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=15.44ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 7610    45.648643/s
    checks_succeeded...: 100.00% 7610 out of 7610
    checks_failed......: 0.00%   0 out of 7610

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=44.4ms   min=2.64ms med=10.44ms p(90)=105.52ms p(95)=124.5ms  p(99)=203.41ms max=1.32s  
      { expected_response:true }...: avg=44.4ms   min=2.64ms med=10.44ms p(90)=105.52ms p(95)=124.5ms  p(99)=203.41ms max=1.32s  
      { name:my_bookings }.........: avg=108.47ms min=48.9ms med=95.71ms p(90)=140.47ms p(95)=176.98ms p(99)=237.25ms max=1.32s  
      { name:resource_slots }......: avg=14.95ms  min=3.86ms med=7.81ms  p(90)=14.77ms  p(95)=17.3ms   p(99)=34.48ms  max=1.25s  
      { name:resources }...........: avg=8.9ms    min=2.64ms med=7.83ms  p(90)=13.3ms   p(95)=15.44ms  p(99)=35.23ms  max=56.63ms
    http_req_failed................: 0.00%  0 out of 3805
      { name:my_bookings }.........: 0.00%  0 out of 1268
      { name:resource_slots }......: 0.00%  0 out of 1268
      { name:resources }...........: 0.00%  0 out of 1268
    http_reqs......................: 3805   22.824321/s

    EXECUTION
    iteration_duration.............: avg=1.13s    min=1.06s  med=1.11s   p(90)=1.18s    p(95)=1.2s     p(99)=1.3s     max=2.35s  
    iterations.....................: 1268   7.606108/s
    vus............................: 1      min=0         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 33 MB  199 kB/s
    data_sent......................: 775 kB 4.6 kB/s
```

## Observations

- 1,268 journeys and 3,805 HTTP requests. All 7,610 checks passed; HTTP failures: 0/3,805.
- Booking-history p95: 176.98 ms (Runs 1–2: 175.01 and 175.96 ms), below the 500 ms target. Resources p95: 15.44 ms; slots p95: 17.3 ms.
- No separate JDBC trace or query plan was captured for this run. The N+1 finding is documented in [Run 1's trace](trace-evidence.md).

[RED dashboard screenshot](<evidence screenshots/run-3-red-dashboard.png>)
