# Booking contention — pessimistic

- Date: 2026-10-22 IST
- Repository commit: `df0560cf57e1dd01d8ebca3dbc1b7e6f6b5f2c70`
- Slot ID: `50001`
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

    checks_total.......: 22      16.091652/s
    checks_succeeded...: 100.00% 22 out of 22
    checks_failed......: 0.00%   0 out of 22

    ✓ login status is 200
    ✓ login sets access token
    ✓ booking response is 201 or 409

    CUSTOM
    booking_conflict_duration......: avg=262.8492 min=235.88   med=261.6843 p(90)=290.62204 p(95)=292.78021 p(99)=295.467322 max=296.1391
    booking_conflicts..............: 19     13.897336/s
    booking_created................: 1      0.731439/s
    booking_created_duration.......: avg=226.7326 min=226.7326 med=226.7326 p(90)=226.7326  p(95)=226.7326  p(99)=226.7326   max=226.7326
    booking_unexpected.............: 0      0/s

    HTTP
    http_req_duration..............: avg=298.32ms min=226.73ms med=261.68ms p(90)=292.4ms   p(95)=296.13ms  p(99)=894.41ms   max=1.04s   
      { expected_response:true }...: avg=298.32ms min=226.73ms med=261.68ms p(90)=292.4ms   p(95)=296.13ms  p(99)=894.41ms   max=1.04s   
    http_req_failed................: 0.00%  0 out of 21
      { name:booking_attempt }.....: 0.00%  0 out of 20
    http_reqs......................: 21     15.360213/s

    EXECUTION
    iteration_duration.............: avg=262.14ms min=229ms    med=261.28ms p(90)=291.65ms  p(95)=294.09ms  p(99)=296.89ms   max=297.59ms
    iterations.....................: 20     14.628775/s
    vus............................: 0      min=0       max=0 
    vus_max........................: 20     min=20      max=20

    NETWORK
    data_received..................: 15 kB  11 kB/s
    data_sent......................: 7.9 kB 5.8 kB/s




running (0m01.4s), 00/20 VUs, 20 complete and 0 interrupted iterations
same_slot ✓ [======================================] 20 VUs  0m00.3s/1m0s  20/20 iters, 1 per VU
```

## Observations

- 1 `201`, 19 `409`, no unexpected responses; all 22 checks passed.
- The 19 conflicts had a 292.78 ms p95. The single successful request took 226.73 ms.
- Database check after the run: slot `50001` is booked and has exactly one confirmed booking.
