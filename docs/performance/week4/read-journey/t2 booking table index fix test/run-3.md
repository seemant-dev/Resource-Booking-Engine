# Read journey — index test, run 3

- Date: 2026-10-21 IST
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
    ✓ 'p(95)<500' p(95)=33.57ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=18.96ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=16.84ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 8234    49.566372/s
    checks_succeeded...: 100.00% 8234 out of 8234
    checks_failed......: 0.00%   0 out of 8234

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=15.65ms min=3.68ms  med=13.96ms p(90)=27.01ms p(95)=30.08ms p(99)=35.12ms max=147.34ms
      { expected_response:true }...: avg=15.65ms min=3.68ms  med=13.96ms p(90)=27.01ms p(95)=30.08ms p(99)=35.12ms max=147.34ms
      { name:my_bookings }.........: avg=23.57ms min=11.97ms med=23.4ms  p(90)=31.45ms p(95)=33.57ms p(99)=37.99ms max=46.86ms 
      { name:resource_slots }......: avg=12.37ms min=5.27ms  med=11.98ms p(90)=16.73ms p(95)=18.96ms p(99)=24.28ms max=29.62ms 
      { name:resources }...........: avg=10.92ms min=3.68ms  med=10.75ms p(90)=15.41ms p(95)=16.84ms p(99)=24.17ms max=28.9ms  
    http_req_failed................: 0.00%  0 out of 4117
      { name:my_bookings }.........: 0.00%  0 out of 1372
      { name:resource_slots }......: 0.00%  0 out of 1372
      { name:resources }...........: 0.00%  0 out of 1372
    http_reqs......................: 4117   24.783186/s

    EXECUTION
    iteration_duration.............: avg=1.04s   min=1.02s   med=1.05s   p(90)=1.06s   p(95)=1.06s   p(99)=1.07s max=1.08s   
    iterations.....................: 1372   8.259055/s
    vus............................: 1      min=1         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 36 MB  216 kB/s
    data_sent......................: 839 kB 5.0 kB/s




running (2m46.1s), 00/10 VUs, 1372 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations

- 1,372 journeys and 4,117 HTTP requests completed. All 8,234 checks passed; HTTP failures: 0/4,117.
- Booking-history p95 was 33.57 ms, below the 500 ms target and the N+1-fix test's 80.01–84.61 ms range. Resources p95: 16.84 ms; slots p95: 18.96 ms.
