package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SlotCandidateGeneratorTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 1);

    private final SlotCandidateGenerator generator = new SlotCandidateGenerator();

    @ParameterizedTest(name = "{0}")
    @MethodSource("generationCases")
    void generatesOnlyCompleteSlotsWithinTheDailyWindow(
            String scenario,
            LocalTime dayStart,
            LocalTime dayEnd,
            int durationMinutes,
            ZoneId businessTimeZone,
            List<SlotWindow> expected
    ) {
        SlotGenerateRequest request = request(dayStart, dayEnd, durationMinutes);

        List<SlotWindow> actual = generator.generate(request, businessTimeZone);

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    private static Stream<Arguments> generationCases() {
        return Stream.of(
                Arguments.of(
                        "exact multiple creates every complete slot",
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0),
                        60,
                        ZoneOffset.UTC,
                        List.of(
                                window("2026-01-01T09:00:00Z", "2026-01-01T10:00:00Z"),
                                window("2026-01-01T10:00:00Z", "2026-01-01T11:00:00Z"),
                                window("2026-01-01T11:00:00Z", "2026-01-01T12:00:00Z")
                        )
                ),
                Arguments.of(
                        "remainder is ignored rather than creating a partial slot",
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 45),
                        60,
                        ZoneOffset.UTC,
                        List.of(window("2026-01-01T09:00:00Z", "2026-01-01T10:00:00Z"))
                ),
                Arguments.of(
                        "slot ending exactly at the boundary is included",
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        60,
                        ZoneOffset.UTC,
                        List.of(window("2026-01-01T09:00:00Z", "2026-01-01T10:00:00Z"))
                ),
                Arguments.of(
                        "window shorter than the duration creates no slot",
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 45),
                        60,
                        ZoneOffset.UTC,
                        List.of()
                ),
                Arguments.of(
                        "business time is converted to UTC",
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        60,
                        ZoneId.of("Asia/Kolkata"),
                        List.of(window("2026-01-01T03:30:00Z", "2026-01-01T04:30:00Z"))
                )
        );
    }

    private static SlotGenerateRequest request(
            LocalTime dayStart,
            LocalTime dayEnd,
            int durationMinutes
    ) {
        SlotGenerateRequest request = new SlotGenerateRequest();
        request.setStartDate(TEST_DATE);
        request.setEndDate(TEST_DATE);
        request.setDayStartTime(dayStart);
        request.setDayEndTime(dayEnd);
        request.setSlotDurationMinutes(durationMinutes);
        return request;
    }

    private static SlotWindow window(String start, String end) {
        return new SlotWindow(Instant.parse(start), Instant.parse(end));
    }
}
