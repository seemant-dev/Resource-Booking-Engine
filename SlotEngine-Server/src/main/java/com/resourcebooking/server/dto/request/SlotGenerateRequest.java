package com.resourcebooking.server.dto.request;

import com.resourcebooking.server.common.SlotGenerationRules;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
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

    @Min(
            value = SlotGenerationRules.MIN_SLOT_DURATION_MINUTES,
            message = "Slot duration must be at least 15 minutes"
    )
    @Max(
            value = SlotGenerationRules.MAX_SLOT_DURATION_MINUTES,
            message = "Slot duration must not exceed 480 minutes"
    )
    private int slotDurationMinutes;

    @AssertTrue(message = "End date must be on or after start date")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Date range must not exceed 31 calendar days")
    public boolean isDateRangeWithinLimit() {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return true;
        }

        return SlotGenerationRules.inclusiveDays(startDate, endDate)
                <= SlotGenerationRules.MAX_DATE_RANGE_DAYS;
    }

    @AssertTrue(message = "Day end time must be after day start time")
    public boolean isTimeRangeValid() {
        if (dayStartTime == null || dayEndTime == null) {
            return true;
        }

        return dayEndTime.isAfter(dayStartTime);
    }

    @AssertTrue(message = "Day start and end times must align to 15-minute intervals")
    public boolean isTimeGridValid() {
        if (dayStartTime == null || dayEndTime == null) {
            return true;
        }

        return SlotGenerationRules.isOnTimeGrid(dayStartTime)
                && SlotGenerationRules.isOnTimeGrid(dayEndTime);
    }

    @AssertTrue(message = "Slot duration must be a multiple of 15 minutes")
    public boolean isSlotDurationGridValid() {
        return slotDurationMinutes <= 0
                || slotDurationMinutes % SlotGenerationRules.TIME_GRID_MINUTES == 0;
    }

    @AssertTrue(message = "Daily time window must divide exactly by the slot duration")
    public boolean isDailyWindowDivisible() {
        if (dayStartTime == null
                || dayEndTime == null
                || !dayEndTime.isAfter(dayStartTime)
                || slotDurationMinutes <= 0) {
            return true;
        }

        return SlotGenerationRules.dailyWindowMinutes(dayStartTime, dayEndTime)
                % slotDurationMinutes == 0;
    }

    @AssertTrue(message = "A generation request must not exceed 1500 slots")
    public boolean isCandidateCountWithinLimit() {
        if (startDate == null
                || endDate == null
                || endDate.isBefore(startDate)
                || dayStartTime == null
                || dayEndTime == null
                || !dayEndTime.isAfter(dayStartTime)
                || slotDurationMinutes <= 0) {
            return true;
        }

        return SlotGenerationRules.countCandidateSlots(
                startDate,
                endDate,
                dayStartTime,
                dayEndTime,
                slotDurationMinutes
        ) <= SlotGenerationRules.MAX_SLOTS_PER_REQUEST;
    }
}
