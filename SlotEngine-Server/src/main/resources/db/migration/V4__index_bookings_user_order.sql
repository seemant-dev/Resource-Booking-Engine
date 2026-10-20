CREATE INDEX idx_bookings_user_booked_at_id
    ON bookings(user_id, booked_at DESC, id DESC);
