package com.resourcebooking.server.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SlotGenerateRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Day start time is required")
    private LocalTime dayStartTime;

    @NotNull(message = "Day end time is required")
    private LocalTime dayEndTime;

    @Min(value = 1, message = "Slot duration must be at least 1 minute")
    private int slotDurationMinutes;

    @AssertTrue(message = "End date must be on or after start date")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Day end time must be after day start time")
    public boolean isTimeRangeValid() {
        if (dayStartTime == null || dayEndTime == null) {
            return true;
        }

        return dayEndTime.isAfter(dayStartTime);
    }
}