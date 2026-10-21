# Read journey — version B, run 3

- Change: `@EntityGraph` fetch fix; original bookings index unchanged
- Approximate run time: 2026-10-21 01:23 IST
- Commit: `f3098d09797129c544b9c0f584be8a4e0af414d3`
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
    ✓ 'p(95)<500' p(95)=83.37ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=16.45ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=15.61ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 7988    48.085624/s
    checks_succeeded...: 100.00% 7988 out of 7988
    checks_failed......: 0.00%   0 out of 7988

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=25.97ms min=2.7ms   med=11.7ms  p(90)=64.22ms p(95)=74.65ms p(99)=87.09ms max=800.86ms
      { expected_response:true }...: avg=25.97ms min=2.7ms   med=11.7ms  p(90)=64.22ms p(95)=74.65ms p(99)=87.09ms max=800.86ms
      { name:my_bookings }.........: avg=57.89ms min=25.49ms med=53.51ms p(90)=78.91ms p(95)=83.37ms p(99)=95.95ms max=225.13ms
      { name:resource_slots }......: avg=10.16ms min=3.69ms  med=9.67ms  p(90)=14.81ms p(95)=16.45ms p(99)=20.55ms max=89.49ms 
      { name:resources }...........: avg=9.29ms  min=2.7ms   med=8.7ms   p(90)=13.95ms p(95)=15.61ms p(99)=21.18ms max=77.25ms 
    http_req_failed................: 0.00%  0 out of 3994
      { name:my_bookings }.........: 0.00%  0 out of 1331
      { name:resource_slots }......: 0.00%  0 out of 1331
      { name:resources }...........: 0.00%  0 out of 1331
    http_reqs......................: 3994   24.042812/s

    EXECUTION
    iteration_duration.............: avg=1.07s   min=1.03s   med=1.07s   p(90)=1.1s    p(95)=1.11s   p(99)=1.13s   max=1.39s   
    iterations.....................: 1331   8.012264/s
    vus............................: 1      min=1         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 35 MB  210 kB/s
    data_sent......................: 813 kB 4.9 kB/s
```

## Observations

- 1,331 journeys and 3,994 HTTP requests. All 7,988 checks passed; HTTP failures: 0/3,994.
- Booking-history p95: 83.37 ms, below the 500 ms target and the version A range of 175.01–176.98 ms. Resources p95: 15.61 ms; slots p95: 16.45 ms.
