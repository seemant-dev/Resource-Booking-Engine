package com.resourcebooking.server.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SlotGenerateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsThirtyOneDaysAndOneThousandFourHundredEightyEightCandidates() {
        SlotGenerateRequest request = request(
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 31),
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                15
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsDateRangeLongerThanThirtyOneDays() {
        SlotGenerateRequest request = request(
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 2, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 15),
                15
        );

        assertThat(messagesFor(validator.validate(request)))
                .containsExactly("Date range must not exceed 31 calendar days");
    }

    @Test
    void rejectsMoreThanOneThousandFiveHundredCandidates() {
        SlotGenerateRequest request = request(
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 19),
                LocalTime.of(0, 0),
                LocalTime.of(19, 45),
                15
        );

        assertThat(messagesFor(validator.validate(request)))
                .containsExactly("A generation request must not exceed 1500 slots");
    }

    private SlotGenerateRequest request(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime dayStartTime,
            LocalTime dayEndTime,
            int durationMinutes
    ) {
        SlotGenerateRequest request = new SlotGenerateRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setDayStartTime(dayStartTime);
        request.setDayEndTime(dayEndTime);
        request.setSlotDurationMinutes(durationMinutes);
        return request;
    }

    private Set<String> messagesFor(Set<ConstraintViolation<SlotGenerateRequest>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}
