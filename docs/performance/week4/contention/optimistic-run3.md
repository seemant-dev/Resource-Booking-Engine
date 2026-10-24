# Booking contention — optimistic, run 3

- Date: 2026-10-22 IST
- Repository commit: `df0560cf57e1dd01d8ebca3dbc1b7e6f6b5f2c70`
- Slot ID: `50005`
- Workload: 20 VUs, one booking attempt each; one setup login
- Command: `& "C:\tools\k6\k6-v2.3.0-windows-amd64\k6.exe" run .\k6\booking-contention.js`

## k6 output

```text
     execution: local
        script: .\k6\booking-contention.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 1m30s max duration (incl. graceful stop):
              * same_slot: 1 iterations for each of 20 VUs (maxDuration: 1m0s, gracefulStop: 30s)



  █ THRESHOLDS 

    booking_conflicts
    ✓ 'count==19' count=19

    booking_created
    ✓ 'count==1' count=1

    booking_unexpected
    ✓ 'count==0' count=0

    checks
    ✓ 'rate==1' rate=100.00%

    http_req_failed{name:booking_attempt}
    ✓ 'rate==0' rate=0.00%

    iterations
    ✓ 'count==20' count=20


  █ TOTAL RESULTS 

    checks_total.......: 22      64.573886/s
    checks_succeeded...: 100.00% 22 out of 22
    checks_failed......: 0.00%   0 out of 22

    ✓ login status is 200
    ✓ login sets access token
    ✓ booking response is 201 or 409

    CUSTOM
    booking_conflict_duration......: avg=80.943789 min=71.6892 med=86.1263 p(90)=88.10708 p(95)=91.57361 p(99)=93.115922 max=93.5015
    booking_conflicts..............: 19     55.768356/s
    booking_created................: 1      2.935177/s
    booking_created_duration.......: avg=58.8695   min=58.8695 med=58.8695 p(90)=58.8695  p(95)=58.8695  p(99)=58.8695   max=58.8695
    booking_unexpected.............: 0      0/s

    HTTP
    http_req_duration..............: avg=86.48ms   min=58.86ms med=86.12ms p(90)=91.35ms  p(95)=93.5ms   p(99)=194.14ms  max=219.3ms
      { expected_response:true }...: avg=86.48ms   min=58.86ms med=86.12ms p(90)=91.35ms  p(95)=93.5ms   p(99)=194.14ms  max=219.3ms
    http_req_failed................: 0.00%  0 out of 21
      { name:booking_attempt }.....: 0.00%  0 out of 20
    http_reqs......................: 21     61.638709/s

    EXECUTION
    iteration_duration.............: avg=81.42ms   min=60.57ms med=82.42ms p(90)=89.28ms  p(95)=92.04ms  p(99)=94.09ms   max=94.6ms 
    iterations.....................: 20     58.703532/s

    NETWORK
    data_received..................: 15 kB  44 kB/s
    data_sent......................: 7.9 kB 23 kB/s




running (0m00.3s), 00/20 VUs, 20 complete and 0 interrupted iterations
same_slot ✓ [======================================] 20 VUs  0m00.1s/1m0s  20/20 iters, 1 per VU
```

## Observations

- 1 `201`, 19 `409`, no unexpected responses; all 22 checks passed.
- The 19 conflicts had a 91.57 ms p95. The single successful request took 58.87 ms.
- Database check after the run: slot `50005` is booked and has exactly one confirmed booking.
