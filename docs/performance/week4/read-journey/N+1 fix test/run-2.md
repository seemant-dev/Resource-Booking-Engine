# Read journey — version B, run 2

- Change: `@EntityGraph` fetch fix; original bookings index unchanged
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
    ✓ 'p(95)<500' p(95)=80.01ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=15.35ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=15.55ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 7976    47.76882/s
    checks_succeeded...: 100.00% 7976 out of 7976
    checks_failed......: 0.00%   0 out of 7976

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=26.64ms min=3.18ms  med=11.18ms p(90)=58.8ms  p(95)=68.99ms p(99)=86.11ms  max=1.25s   
      { expected_response:true }...: avg=26.64ms min=3.18ms  med=11.18ms p(90)=58.8ms  p(95)=68.99ms p(99)=86.11ms  max=1.25s   
      { name:my_bookings }.........: avg=58.16ms min=26.38ms med=52.16ms p(90)=72.81ms p(95)=80.01ms p(99)=119.57ms max=755.21ms
      { name:resource_slots }......: avg=9.81ms  min=4.33ms  med=8.98ms  p(90)=13.64ms p(95)=15.35ms p(99)=20.62ms  max=64.31ms 
      { name:resources }...........: avg=11.02ms min=3.18ms  med=8.35ms  p(90)=13.7ms  p(95)=15.55ms p(99)=30.58ms  max=566.21ms
    http_req_failed................: 0.00%  0 out of 3988
      { name:my_bookings }.........: 0.00%  0 out of 1329
      { name:resource_slots }......: 0.00%  0 out of 1329
      { name:resources }...........: 0.00%  0 out of 1329
    http_reqs......................: 3988   23.88441/s

    EXECUTION
    iteration_duration.............: avg=1.08s   min=1.04s   med=1.07s   p(90)=1.1s    p(95)=1.1s    p(99)=1.15s    max=1.78s   
    iterations.....................: 1329   7.959474/s
    vus............................: 1      min=0         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 35 MB  208 kB/s
    data_sent......................: 812 kB 4.9 kB/s




running (2m47.0s), 00/10 VUs, 1329 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations

- 1,329 journeys and 3,988 HTTP requests. All 7,976 checks passed; HTTP failures: 0/3,988.
- Booking-history p95: 80.01 ms, below the 500 ms target and the version A range of 175.01–176.98 ms. Resources p95: 15.55 ms; slots p95: 15.35 ms.
