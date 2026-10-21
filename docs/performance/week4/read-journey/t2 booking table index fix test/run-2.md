# Read journey — index test, run 2

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
    ✓ 'p(95)<500' p(95)=36.54ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=18.21ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=18.8ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 8216    48.952775/s
    checks_succeeded...: 100.00% 8216 out of 8216
    checks_failed......: 0.00%   0 out of 8216

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=16.49ms min=3.72ms  med=13.76ms p(90)=27.43ms p(95)=31.64ms p(99)=40.02ms max=1.76s   
      { expected_response:true }...: avg=16.49ms min=3.72ms  med=13.76ms p(90)=27.43ms p(95)=31.64ms p(99)=40.02ms max=1.76s   
      { name:my_bookings }.........: avg=24.68ms min=11.87ms med=21.92ms p(90)=33.89ms p(95)=36.54ms p(99)=42.55ms max=606.13ms
      { name:resource_slots }......: avg=11.73ms min=4.91ms  med=10.72ms p(90)=16.39ms p(95)=18.21ms p(99)=26.47ms max=179.85ms
      { name:resources }...........: avg=11.78ms min=3.72ms  med=11.05ms p(90)=15.69ms p(95)=18.8ms  p(99)=29.89ms max=179.54ms
    http_req_failed................: 0.00%  0 out of 4108
      { name:my_bookings }.........: 0.00%  0 out of 1369
      { name:resource_slots }......: 0.00%  0 out of 1369
      { name:resources }...........: 0.00%  0 out of 1369
    http_reqs......................: 4108   24.476387/s

    EXECUTION
    iteration_duration.............: avg=1.05s   min=1.02s   med=1.04s   p(90)=1.06s   p(95)=1.07s   p(99)=1.09s max=1.61s   
    iterations.....................: 1369   8.15681/s
    vus............................: 1      min=0         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 36 MB  213 kB/s
    data_sent......................: 837 kB 5.0 kB/s




running (2m47.8s), 00/10 VUs, 1369 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations

- 1,369 journeys and 4,108 HTTP requests completed. All 8,216 checks passed; HTTP failures: 0/4,108.
- Booking-history p95 was 36.54 ms, below the 500 ms target and the N+1-fix test's 80.01–84.61 ms range. Resources p95: 18.8 ms; slots p95: 18.21 ms.
