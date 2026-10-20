-- Local performance-test data.
-- Seeded login password: SlotEnginePerf123!

BEGIN;

SET LOCAL TIME ZONE 'UTC';

-- Reset application data without touching Flyway migration history.
TRUNCATE TABLE
    refresh_tokens,
    bookings,
    slots,
    users,
    resources
RESTART IDENTITY CASCADE;

-- 100 resources: resource1 through resource100.
INSERT INTO resources (name, description, created_at)
SELECT
    'resource' || resource_number,
    'Performance test resource',
    TIMESTAMPTZ '2029-12-01 00:00:00+00'
FROM generate_series(1, 100) AS resource_number;

-- 499 users plus one admin = 500 users.
INSERT INTO users (name, email, password_hash, role, created_at)
SELECT
    CASE
        WHEN user_number = 1 THEN 'Heavy Load User'
        ELSE 'Performance User ' || user_number
    END,
    'user' || user_number || '@mailinator.com',
    '$2a$10$m9HsBOb4nj/QSN3S6v3zNuP4OLOe/KmoBwT/X5tV6WdkYBrXrnoFq',
    'USER',
    TIMESTAMPTZ '2029-12-01 00:00:00+00'
FROM generate_series(1, 499) AS user_number
UNION ALL
SELECT
    'Performance Admin',
    'admin@mailinator.com',
    '$2a$10$m9HsBOb4nj/QSN3S6v3zNuP4OLOe/KmoBwT/X5tV6WdkYBrXrnoFq',
    'ADMIN',
    TIMESTAMPTZ '2029-12-01 00:00:00+00';

-- 600 half-hour slots per resource across 30 days, from 08:00 to 18:00 UTC.
-- The first 500 slots per resource are booked; the final 100 are available.
WITH seeded_resources AS (
    SELECT
        id,
        REPLACE(name, 'resource', '')::INTEGER AS resource_number
    FROM resources
)
INSERT INTO slots (
    resource_id,
    slot_start,
    slot_end,
    is_booked,
    version,
    created_at
)
SELECT
    resource.id,
    TIMESTAMPTZ '2030-01-01 08:00:00+00'
        + ((slot_number - 1) / 20) * INTERVAL '1 day'
        + ((slot_number - 1) % 20) * INTERVAL '30 minutes',
    TIMESTAMPTZ '2030-01-01 08:30:00+00'
        + ((slot_number - 1) / 20) * INTERVAL '1 day'
        + ((slot_number - 1) % 20) * INTERVAL '30 minutes',
    slot_number <= 500,
    CASE WHEN slot_number <= 500 THEN 1 ELSE 0 END,
    TIMESTAMPTZ '2029-12-01 00:00:00+00'
FROM seeded_resources resource
CROSS JOIN generate_series(1, 600) AS slot_number;

-- 10,000 bookings belong to user1; 40,000 are shared by users 2-499.
WITH numbered_slots AS (
    SELECT
        s.id AS slot_id,
        REPLACE(r.name, 'resource', '')::INTEGER AS resource_number,
        ROW_NUMBER() OVER (
            PARTITION BY r.id
            ORDER BY s.slot_start
        )::INTEGER AS slot_number
    FROM slots s
    JOIN resources r ON r.id = s.resource_id
),
booking_assignments AS (
    SELECT
        slot_id,
        resource_number,
        slot_number,
        CASE
            WHEN slot_number <= 100 THEN 'user1@mailinator.com'
            ELSE 'user'
                || (((resource_number - 1) * 400 + slot_number - 101) % 498 + 2)
                || '@mailinator.com'
        END AS user_email
    FROM numbered_slots
    WHERE slot_number <= 500
)
INSERT INTO bookings (slot_id, user_id, status, booked_at, cancelled_at)
SELECT
    assignment.slot_id,
    user_account.id,
    'CONFIRMED',
    TIMESTAMPTZ '2029-12-15 00:00:00+00'
        + (((assignment.resource_number - 1) * 500 + assignment.slot_number - 1)
            * INTERVAL '1 second'),
    NULL
FROM booking_assignments assignment
JOIN users user_account ON user_account.email = assignment.user_email;

COMMIT;

-- Expected: 100, 500, 60000, 50000, 10000, 50000, 10000.
SELECT
    (SELECT COUNT(*) FROM resources) AS resources,
    (SELECT COUNT(*) FROM users) AS users,
    (SELECT COUNT(*) FROM slots) AS slots,
    (SELECT COUNT(*) FROM slots WHERE is_booked) AS booked_slots,
    (SELECT COUNT(*) FROM slots WHERE NOT is_booked) AS available_slots,
    (SELECT COUNT(*) FROM bookings) AS bookings,
    (
        SELECT COUNT(*)
        FROM bookings b
        JOIN users u ON u.id = b.user_id
        WHERE u.email = 'user1@mailinator.com'
    ) AS heavy_user_bookings;
