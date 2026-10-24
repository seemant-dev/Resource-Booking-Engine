# Booking contention — optimistic

- Date: 2026-10-22 IST
- Repository commit: `df0560cf57e1dd01d8ebca3dbc1b7e6f6b5f2c70`
- Slot ID: `60000`
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

    checks_total.......: 22      5.023335/s
    checks_succeeded...: 100.00% 22 out of 22
    checks_failed......: 0.00%   0 out of 22

    ✓ login status is 200
    ✓ login sets access token
    ✓ booking response is 201 or 409

    CUSTOM
    booking_conflict_duration......: avg=395.337495 min=351.3769 med=396.5573 p(90)=409.59384 p(95)=410.49758 p(99)=413.864156 max=414.7058
    booking_conflicts..............: 19     4.338335/s
    booking_created................: 1      0.228333/s
    booking_created_duration.......: avg=321.0204   min=321.0204 med=321.0204 p(90)=321.0204  p(95)=321.0204  p(99)=321.0204   max=321.0204
    booking_unexpected.............: 0      0/s

    HTTP
    http_req_duration..............: avg=559.71ms   min=321.02ms med=396.55ms p(90)=410.03ms  p(95)=414.7ms   p(99)=3.22s      max=3.92s   
      { expected_response:true }...: avg=559.71ms   min=321.02ms med=396.55ms p(90)=410.03ms  p(95)=414.7ms   p(99)=3.22s      max=3.92s   
    http_req_failed................: 0.00%  0 out of 21
      { name:booking_attempt }.....: 0.00%  0 out of 20
    http_reqs......................: 21     4.795001/s

    EXECUTION
    iteration_duration.............: avg=396.46ms   min=324.94ms med=401.87ms p(90)=416.02ms  p(95)=416.04ms  p(99)=416.27ms   max=416.33ms
    iterations.....................: 20     4.566668/s
    vus............................: 20     min=0       max=20
    vus_max........................: 20     min=20      max=20

    NETWORK
    data_received..................: 15 kB  3.4 kB/s
    data_sent......................: 7.9 kB 1.8 kB/s
```

## Observations

- 1 `201`, 19 `409`, no unexpected responses; all 22 checks passed.
- The 19 conflicts had a 410.50 ms p95. The single successful request took 321.02 ms.
- Database check after the run: slot `60000` is booked and has exactly one confirmed booking.
