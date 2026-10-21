# Read journey — version B, run 1

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
    ✓ 'p(95)<500' p(95)=84.61ms

    http_req_duration{name:resource_slots}
    ✓ 'p(95)<500' p(95)=19.06ms

    http_req_duration{name:resources}
    ✓ 'p(95)<500' p(95)=19.7ms

    http_req_failed{name:my_bookings}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resource_slots}
    ✓ 'rate<0.01' rate=0.00%

    http_req_failed{name:resources}
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 7856    47.122898/s
    checks_succeeded...: 100.00% 7856 out of 7856
    checks_failed......: 0.00%   0 out of 7856

    ✓ login status is 200
    ✓ login sets access token
    ✓ resources status is 200
    ✓ target resource exists
    ✓ slots status is 200
    ✓ bookings status is 200
    ✓ bookings page contains 50 records
    ✓ bookings metadata matches seeded data

    HTTP
    http_req_duration..............: avg=32.93ms min=3.76ms  med=13.19ms p(90)=66.57ms p(95)=74.19ms p(99)=95.86ms  max=1.92s
      { expected_response:true }...: avg=32.93ms min=3.76ms  med=13.19ms p(90)=66.57ms p(95)=74.19ms p(99)=95.86ms  max=1.92s
      { name:my_bookings }.........: avg=69.2ms  min=30.54ms med=58.86ms p(90)=77.44ms p(95)=84.61ms p(99)=231.62ms max=1.92s
      { name:resource_slots }......: avg=15.75ms min=4.55ms  med=10.21ms p(90)=16.44ms p(95)=19.06ms p(99)=29.95ms  max=1.87s
      { name:resources }...........: avg=12.7ms  min=3.76ms  med=10.09ms p(90)=16.88ms p(95)=19.7ms  p(99)=29.24ms  max=1.86s
    http_req_failed................: 0.00%  0 out of 3928
      { name:my_bookings }.........: 0.00%  0 out of 1309
      { name:resource_slots }......: 0.00%  0 out of 1309
      { name:resources }...........: 0.00%  0 out of 1309
    http_reqs......................: 3928   23.561449/s

    EXECUTION
    iteration_duration.............: avg=1.1s    min=1.04s   med=1.08s   p(90)=1.1s    p(95)=1.12s   p(99)=1.28s    max=2.94s
    iterations.....................: 1309   7.851817/s
    vus............................: 1      min=0         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 34 MB  205 kB/s
    data_sent......................: 800 kB 4.8 kB/s




running (2m46.7s), 00/10 VUs, 1309 complete and 0 interrupted iterations
default ✓ [======================================] 00/10 VUs  2m45s
```

## Observations

- 1,309 journeys and 3,928 HTTP requests. All 7,856 checks passed; HTTP failures: 0/3,928.
- Booking-history p95: 84.61 ms, below the 500 ms target and the version A range of 175.01–176.98 ms. Resources p95: 19.7 ms; slots p95: 19.06 ms.
