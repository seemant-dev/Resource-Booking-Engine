package com.resourcebooking.server.common;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class SlotGenerationRules {

    public static final int TIME_GRID_MINUTES = 15;
    public static final int MIN_SLOT_DURATION_MINUTES = 15;
    public static final int MAX_SLOT_DURATION_MINUTES = 480;
    public static final int MAX_DATE_RANGE_DAYS = 31;
    public static final int MAX_SLOTS_PER_REQUEST = 1_500;

    private SlotGenerationRules() {
    }

    public static boolean isOnTimeGrid(LocalTime time) {
        return time.getMinute() % TIME_GRID_MINUTES == 0
                && time.getSecond() == 0
                && time.getNano() == 0;
    }

    public static long inclusiveDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public static long dailyWindowMinutes(LocalTime startTime, LocalTime endTime) {
        return Duration.between(startTime, endTime).toMinutes();
    }

    public static long countCandidateSlots(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            int slotDurationMinutes
    ) {
        return inclusiveDays(startDate, endDate)
                * (dailyWindowMinutes(startTime, endTime) / slotDurationMinutes);
    }
}
