# Performance database profile

Captured after running `seed-performance-data.sql` twice and refreshing PostgreSQL statistics with `ANALYZE`.

## Row distribution

| Data                    |  Count |
| ----------------------- | -----: |
| Resources               |    100 |
| Users                   |    500 |
| Slots                   | 60,000 |
| Booked slots            | 50,000 |
| Available slots         | 10,000 |
| Bookings                | 50,000 |
| Heavy-user bookings     | 10,000 | (across all resources)
| Refresh tokens          |      0 |

- Every resource has 600 slots.
- Normal users have 80 or 81 bookings each.

## Database size

- Database: `resource_booking_db`
- Total size: 23 MB
- Slots: 8,488 kB
- Bookings: 6,720 kB
- Users: 200 kB
- Resources: 64 kB
- Refresh tokens: 40 kB

## Existing indexes

| Table            | Index                           | Columns                                      |
| ---------------- | ------------------------------- | -------------------------------------------- |
| `resources`      | `resources_pkey`                | `id`                                         |
| `users`          | `users_pkey`                    | `id`                                         |
| `users`          | `users_email_key`               | `email` (unique)                             |
| `slots`          | `slots_pkey`                    | `id`                                         |
| `slots`          | `uk_slots_resource_start`       | `resource_id, slot_start` (unique)            |
| `bookings`       | `bookings_pkey`                 | `id`                                         |
| `bookings`       | `idx_bookings_user`             | `user_id`                                    |
| `bookings`       | `uk_bookings_confirmed_slot`    | `slot_id` for confirmed bookings (unique)    |
| `refresh_tokens` | `refresh_tokens_pkey`           | `id`                                         |
| `refresh_tokens` | `refresh_tokens_token_key`      | `token` (unique)                             |
| `refresh_tokens` | `idx_refresh_tokens_user`       | `user_id`                                    |
| `refresh_tokens` | `idx_refresh_tokens_expires_at` | `expires_at`                                 |

The slot lookup already has an index on `(resource_id, slot_start)`. Booking history has an index on `user_id`, but no index supporting both user filtering and `booked_at` ordering.