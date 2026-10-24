# Booking contention — pessimistic, run 3

- Date: 2026-10-22 IST
- Repository commit: `df0560cf57e1dd01d8ebca3dbc1b7e6f6b5f2c70`
- Slot ID: `50003`
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

    checks_total.......: 22      80.268183/s
    checks_succeeded...: 100.00% 22 out of 22
    checks_failed......: 0.00%   0 out of 22

    ✓ login status is 200
    ✓ login sets access token
    ✓ booking response is 201 or 409

    CUSTOM
    booking_conflict_duration......: avg=97.900295 min=63.7746 med=97.7295 p(90)=125.46516 p(95)=130.5932 p(99)=131.3924 max=131.5922
    booking_conflicts..............: 19     69.322522/s
    booking_created................: 1      3.648554/s
    booking_created_duration.......: avg=57.8516   min=57.8516 med=57.8516 p(90)=57.8516   p(95)=57.8516  p(99)=57.8516  max=57.8516 
    booking_unexpected.............: 0      0/s

    HTTP
    http_req_duration..............: avg=97.01ms   min=57.85ms med=97.72ms p(90)=124.21ms  p(95)=130.48ms p(99)=131.37ms max=131.59ms
      { expected_response:true }...: avg=97.01ms   min=57.85ms med=97.72ms p(90)=124.21ms  p(95)=130.48ms p(99)=131.37ms max=131.59ms
    http_req_failed................: 0.00%  0 out of 21
      { name:booking_attempt }.....: 0.00%  0 out of 20
    http_reqs......................: 21     76.61963/s

    EXECUTION
    iteration_duration.............: avg=96.9ms    min=59.81ms med=95.8ms  p(90)=125.43ms  p(95)=131.94ms p(99)=132.06ms max=132.09ms
    iterations.....................: 20     72.971076/s

    NETWORK
    data_received..................: 15 kB  55 kB/s
    data_sent......................: 7.9 kB 29 kB/s




running (0m00.3s), 00/20 VUs, 20 complete and 0 interrupted iterations
same_slot ✓ [======================================] 20 VUs  0m00.1s/1m0s  20/20 iters, 1 per VU
```

## Observations

- 1 `201`, 19 `409`, no unexpected responses; all 22 checks passed.
- The 19 conflicts had a 130.59 ms p95. The single successful request took 57.85 ms.
- Database check after the run: slot `50003` is booked and has exactly one confirmed booking.
