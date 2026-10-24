# Booking contention — optimistic, run 2

- Date: 2026-10-22 IST
- Repository commit: `df0560cf57e1dd01d8ebca3dbc1b7e6f6b5f2c70`
- Slot ID: `50004`
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

    checks_total.......: 22      15.58494/s
    checks_succeeded...: 100.00% 22 out of 22
    checks_failed......: 0.00%   0 out of 22

    ✓ login status is 200
    ✓ login sets access token
    ✓ booking response is 201 or 409

    CUSTOM
    booking_conflict_duration......: avg=227.501795 min=213.5455 med=225.0267 p(90)=235.4818 p(95)=236.4482 p(99)=236.4482 max=236.4482
    booking_conflicts..............: 19     13.459721/s
    booking_created................: 1      0.708406/s
    booking_created_duration.......: avg=196.0029   min=196.0029 med=196.0029 p(90)=196.0029 p(95)=196.0029 p(99)=196.0029 max=196.0029
    booking_unexpected.............: 0      0/s

    HTTP
    http_req_duration..............: avg=269.39ms   min=196ms    med=225.02ms p(90)=236.44ms p(95)=236.44ms p(99)=958.25ms max=1.13s   
      { expected_response:true }...: avg=269.39ms   min=196ms    med=225.02ms p(90)=236.44ms p(95)=236.44ms p(99)=958.25ms max=1.13s   
    http_req_failed................: 0.00%  0 out of 21
      { name:booking_attempt }.....: 0.00%  0 out of 20
    http_reqs......................: 21     14.876533/s

    EXECUTION
    iteration_duration.............: avg=231.09ms   min=202.95ms med=229.27ms p(90)=239.72ms p(95)=240.69ms p(99)=240.69ms max=240.69ms
    iterations.....................: 20     14.168127/s
    vus............................: 0      min=0       max=0 
    vus_max........................: 20     min=20      max=20

    NETWORK
    data_received..................: 15 kB  11 kB/s
    data_sent......................: 7.9 kB 5.6 kB/s




running (0m01.4s), 00/20 VUs, 20 complete and 0 interrupted iterations
same_slot ✓ [======================================] 20 VUs  0m00.2s/1m0s  20/20 iters, 1 per VU
```

## Observations

- 1 `201`, 19 `409`, no unexpected responses; all 22 checks passed.
- The 19 conflicts had a 236.45 ms p95. The single successful request took 196.00 ms.
- Database check after the run: slot `50004` is booked and has exactly one confirmed booking.
