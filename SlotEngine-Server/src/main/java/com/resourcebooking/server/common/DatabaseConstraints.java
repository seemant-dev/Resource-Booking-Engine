package com.resourcebooking.server.common;

public final class DatabaseConstraints {

    private DatabaseConstraints() {
    }

    public static final String BOOKINGS_CONFIRMED_SLOT_UNIQUE = "uk_bookings_confirmed_slot";
    public static final String SLOTS_RESOURCE_START_UNIQUE = "uk_slots_resource_start";
    public static final String SLOTS_RESOURCE_TIME_EXCLUSION = "ex_slots_resource_time_overlap";
}
