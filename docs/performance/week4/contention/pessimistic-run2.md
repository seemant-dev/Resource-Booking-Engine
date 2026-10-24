# Booking contention — pessimistic, run 2

- Date: 2026-10-22 IST
- Repository commit: `df0560cf57e1dd01d8ebca3dbc1b7e6f6b5f2c70`
- Slot ID: `50002`
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

    checks_total.......: 22      63.80851/s
    checks_succeeded...: 100.00% 22 out of 22
    checks_failed......: 0.00%   0 out of 22

    ✓ login status is 200
    ✓ login sets access token
    ✓ booking response is 201 or 409

    CUSTOM
    booking_conflict_duration......: avg=149.533753 min=115.6471 med=148.2827 p(90)=181.35484 p(95)=184.49026 p(99)=187.701892 max=188.5048
    booking_conflicts..............: 19     55.107349/s
    booking_created................: 1      2.900387/s
    booking_created_duration.......: avg=110.1658   min=110.1658 med=110.1658 p(90)=110.1658  p(95)=110.1658  p(99)=110.1658   max=110.1658
    booking_unexpected.............: 0      0/s

    HTTP
    http_req_duration..............: avg=146.46ms   min=110.16ms med=144.81ms p(90)=180.68ms  p(95)=184.04ms  p(99)=187.61ms   max=188.5ms 
      { expected_response:true }...: avg=146.46ms   min=110.16ms med=144.81ms p(90)=180.68ms  p(95)=184.04ms  p(99)=187.61ms   max=188.5ms 
    http_req_failed................: 0.00%  0 out of 21
      { name:booking_attempt }.....: 0.00%  0 out of 20
    http_reqs......................: 21     60.908123/s

    EXECUTION
    iteration_duration.............: avg=148.83ms   min=111.89ms med=147.41ms p(90)=182.45ms  p(95)=185.5ms   p(99)=189.63ms   max=190.67ms
    iterations.....................: 20     58.007736/s

    NETWORK
    data_received..................: 15 kB  43 kB/s
    data_sent......................: 7.9 kB 23 kB/s




running (0m00.3s), 00/20 VUs, 20 complete and 0 interrupted iterations
same_slot ✓ [======================================] 20 VUs  0m00.2s/1m0s  20/20 iters, 1 per VU
```

## Observations

- 1 `201`, 19 `409`, no unexpected responses; all 22 checks passed.
- The 19 conflicts had a 184.49 ms p95. The single successful request took 110.17 ms.
- Database check after the run: slot `50002` is booked and has exactly one confirmed booking.
