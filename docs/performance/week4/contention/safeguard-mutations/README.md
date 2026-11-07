# Booking concurrency safeguard mutations

## Goal

Verify how `@Version`, the pessimistic row lock, and the confirmed-booking
unique index prevent multiple bookings for one slot.

Each strategy receives 20 concurrent attempts against one available slot in a
clean PostgreSQL 16 Testcontainer.

## Experiment matrix

| State | Optimistic strategy | Pessimistic strategy | Confirmed-booking unique index | Status |
|---|---|---|---|---|
| All safeguards present | `@Version` enabled | Row lock enabled | Present | Complete |
| Application safeguards only | `@Version` enabled | Row lock enabled | Removed | Complete |
| Database safeguard only | `@Version` removed | Row lock removed | Present | Complete |
| No safeguards | `@Version` removed | Row lock removed | Removed | Complete |

Protected states must produce one successful booking, 19 recognized conflicts,
one confirmed database row, and zero unexpected failures. The no-safeguards
state is expected to fail this rule.

## Command

Run from `SlotEngine-Server`:

```powershell
mvn "-Dtest=OptimisticBookingConcurrencyIntegrationTest,PessimisticBookingConcurrencyIntegrationTest" test
```

## 1. All safeguards present

### Environment

| Setting | Value |
|---|---|
| Date recorded | 2026-11-02 |
| Database | PostgreSQL 16 Testcontainer |
| Attempts per strategy | 20 concurrent threads |
| Target | One newly created available slot |
| Strategy execution | Sequential; attempts within each strategy are concurrent |
| Database cleanup | Tables truncated and identities reset before each test |

### Results

| Measurement | Optimistic | Pessimistic |
|---|---:|---:|
| Successful bookings | 1 | 1 |
| Failure target (20 attempts - 1 booking) | 19 | 19 |
| Recognized conflict failures observed | 19 | 19 |
| Slot-already-booked failures | 10 | 19 |
| Optimistic-lock conflicts | 0 | 0 |
| Database-integrity conflicts | 9 | 0 |
| Unexpected failures | 0 | 0 |
| Confirmed database bookings | 1 | 1 |
| Final `slot.isBooked` | `true` | `true` |
| Test result | Pass | Pass |
| Test elapsed time | 18.34 s | 1.469 s |

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 25.509 s
```

Result: the complete production protection enforced one booking.

## 2. Application safeguards only

Setup: the Testcontainer unique index was dropped after Flyway. `@Version` and
the pessimistic row lock remained enabled. V2 and the development database were
not modified.

### Results

| Measurement | Optimistic | Pessimistic |
|---|---:|---:|
| Successful bookings | 1 | 1 |
| Failure target (20 attempts - 1 booking) | 19 | 19 |
| Recognized conflict failures observed | 19 | 19 |
| Slot-already-booked failures | 10 | 19 |
| Optimistic-lock conflicts | 9 | 0 |
| Database-integrity conflicts | 0 | 0 |
| Unexpected failures | 0 | 0 |
| Confirmed database bookings | 1 | 1 |
| Final `slot.isBooked` | `true` | `true` |
| Test result | Pass | Pass |
| Test elapsed time | 18.03 s | 1.469 s |

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 28.775 s
```

Result: `@Version` protected optimistic booking, and the row lock protected
pessimistic booking without the database backstop.

## 3. Database safeguard only

Setup: `@Version` and the pessimistic row lock were removed. A fresh container
kept the unique index created by Flyway V2.

### Results

| Measurement | Optimistic | Pessimistic |
|---|---:|---:|
| Successful bookings | 1 | 1 |
| Failure target (20 attempts - 1 booking) | 19 | 19 |
| Recognized conflict failures observed | 19 | 19 |
| Slot-already-booked failures | 10 | 10 |
| Optimistic-lock conflicts | 0 | 0 |
| Database-integrity conflicts | 9 | 9 |
| Unexpected failures | 0 | 0 |
| Confirmed database bookings | 1 | 1 |
| Final `slot.isBooked` | `true` | `true` |
| Test result | Pass | Pass |
| Test elapsed time | 17.27 s | 1.337 s |

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 27.794 s
```

Result: the unique index alone enforced one confirmed booking for both strategy
settings.

## 4. No safeguards

Setup: `@Version` and the pessimistic row lock were removed, and the unique
index was dropped inside the Testcontainer.

### Results

| Measurement | Optimistic | Pessimistic |
|---|---:|---:|
| Successful bookings | 10 | 10 |
| Failure target (20 attempts - 1 booking) | 19 | 19 |
| Recognized conflict failures observed | 10 | 10 |
| Slot-already-booked failures | 10 | 10 |
| Optimistic-lock conflicts | 0 | 0 |
| Database-integrity conflicts | 0 | 0 |
| Unexpected failures | 0 | 0 |
| Confirmed database bookings | 10 | 10 |
| Final `slot.isBooked` | `true` | `true` |
| Test result | Expected failure | Expected failure |
| Test-class elapsed time | 17.76 s | 1.525 s |

```text
Tests run: 2, Failures: 2, Errors: 0, Skipped: 0
BUILD FAILURE (expected mutation result)
Total time: 25.086 s
```

Result: both tests detected double booking with ten bookings and ten confirmed
rows. No stronger synchronization was required for this run.

## Conclusion

| Protection state | Outcome |
|---|---|
| All safeguards | One booking |
| Application safeguards only | One booking |
| Database safeguard only | One booking |
| No safeguards | Ten bookings; tests failed as expected |

The application safeguards and database backstop each independently prevented
multiple confirmed bookings in these runs. Keeping both provides defense in
depth.
