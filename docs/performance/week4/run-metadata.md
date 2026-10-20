# Read-journey benchmark metadata run1

Prepared: 2026-10-19 IST. Run timestamps go in `run-1.md`, `run-2.md`, and `run-3.md`.

| Item                                 | Value                                                                                     |
| ------------------------------------ | ----------------------------------------------------------------------------------------- |
| Source commit (before run 1)         | `9f18dd6251c4dd1f754475accf445f33a3fb33ac`                                                |
| Benchmark tag                        | `benchmark/read-path-initial`                                                             |
| Latest commit label                  | `v0.4.7` (`perf`: k6 read journey)                                                        |
| Application change                   | `v0.4.6` (`feat`: paginated booking history)                                              |
| Maven artifact                       | `0.0.1-SNAPSHOT`                                                                          |
| Dataset                              | 100 resources, 500 users, 60,000 slots, 50,000 bookings                                   |
| Heavy user                           | `user1@mailinator.com`, 10,000 bookings                                                   |
| Dataset details and existing indexes | [Database profile](setup/database-profile.md)                                             |
| Execution                            | Local k6 against the observed SlotEngine application and local PostgreSQL                 |
| k6                                   | `v2.3.0`, Windows AMD64; `C:\tools\k6\k6-v2.3.0-windows-amd64\k6.exe`                     |

## Test configuration

- `GET /api/resources` → `GET /api/resources/{id}/slots?date=2030-01-01` → `GET /api/bookings/me?page=0`.
- Login runs once during k6 setup; all VUs use the heavy user's access token.
- Ramp up to 10 VUs over 30 seconds, hold for 120 seconds, ramp down over 15 seconds; each iteration sleeps 1 second.
- Booking page size: 50. Three measured runs with the same script, seed, application launch mode, and machine power mode.
- Per-endpoint targets: p95 below 500 ms and HTTP failure rate below 1%; all functional checks pass.
- k6 provides client latency and throughput; Grafana traces provide server and JDBC evidence.

The 10-VU load and latency target are controlled experiment settings, not measured production traffic or a production SLO.

## Fixed comparison constraints

The following must be identical for a before/after pair:

- Same machine and power mode.
- Same Git commit except for the isolated fix under test.
- Same Java, PostgreSQL, and k6 versions.
- Same application launch mode and JVM memory configuration.
- Same OpenTelemetry configuration and sampling behaviour.
- Same database seed and row distribution.
- Same k6 script revision, execution mode, VUs, stages, page size, and thresholds.
- Same resource ID, user ID, date range, and target slot preparation rules.
- Same warm-up procedure before each measured run.
- No simultaneous builds, test suites, database clients, or duplicate application instances.
- Record all runs; do not select only the fastest result.
