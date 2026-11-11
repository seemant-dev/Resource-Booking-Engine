package com.resourcebooking.server.integration.slot;

import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.integration.AbstractBookingApiIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SlotGenerationApiIntegrationTest extends AbstractBookingApiIntegrationTest {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Kolkata");

    private Resource resource;
    private Cookie adminAccessToken;

    @BeforeEach
    void setUpAdminAndResource() throws Exception {
        User admin = createUser("Slot Generation Admin", Role.ADMIN);
        resource = createResource("Slot Generation API Room");
        adminAccessToken = login(admin);
    }

    @Test
    void remainderWindowShouldPersistOnlyCompleteSlotsAsUtcInstants() throws Exception {
        LocalDate date = futureBusinessDate();

        generateSlots(date, date, LocalTime.of(9, 0), LocalTime.of(10, 45), 60)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generated").value(1))
                .andExpect(jsonPath("$.skipped").value(0));

        assertThat(savedSlots()).singleElement().satisfies(slot -> {
            assertThat(slot.getSlotStart()).isEqualTo(instant(date, 9, 0));
            assertThat(slot.getSlotEnd()).isEqualTo(instant(date, 10, 0));
        });
    }

    @Test
    void identicalSecondGenerationShouldSkipEveryCandidate() throws Exception {
        LocalDate date = futureBusinessDate();

        generateSlots(date, date, LocalTime.of(9, 0), LocalTime.of(11, 0), 60)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generated").value(2))
                .andExpect(jsonPath("$.skipped").value(0));

        generateSlots(date, date, LocalTime.of(9, 0), LocalTime.of(11, 0), 60)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generated").value(0))
                .andExpect(jsonPath("$.skipped").value(2));

        assertThat(savedSlots()).hasSize(2);
    }

    @Test
    void partialOverlapShouldPersistOnlyAvailableCandidates() throws Exception {
        LocalDate date = futureBusinessDate();
        createExistingSlot(instant(date, 10, 0), instant(date, 11, 0));

        generateSlots(date, date, LocalTime.of(9, 0), LocalTime.of(12, 0), 60)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generated").value(2))
                .andExpect(jsonPath("$.skipped").value(1));

        assertThat(savedSlots())
                .extracting(Slot::getSlotStart)
                .containsExactly(
                        instant(date, 9, 0),
                        instant(date, 10, 0),
                        instant(date, 11, 0)
                );
    }

    @Test
    void durationOutsideFifteenMinuteGridShouldReturnBadRequestWithoutPersistingSlots()
            throws Exception {
        LocalDate date = futureBusinessDate();

        generateSlots(date, date, LocalTime.of(9, 0), LocalTime.of(10, 0), 20)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertThat(slotRepository.count()).isZero();
    }

    @Test
    void dateRangeOverThirtyOneDaysShouldReturnBadRequestWithoutPersistingSlots()
            throws Exception {
        LocalDate startDate = futureBusinessDate();

        generateSlots(
                startDate,
                startDate.plusDays(31),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                60
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertThat(slotRepository.count()).isZero();
    }

    @Test
    void requestOverSlotCountLimitShouldReturnBadRequestWithoutPersistingSlots()
            throws Exception {
        LocalDate startDate = futureBusinessDate();

        generateSlots(
                startDate,
                startDate.plusDays(18),
                LocalTime.MIDNIGHT,
                LocalTime.of(19, 45),
                15
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertThat(slotRepository.count()).isZero();
    }

    @Test
    void pastStartDateShouldReturnBadRequestWithoutPersistingSlots() throws Exception {
        LocalDate pastDate = LocalDate.now(BUSINESS_TIME_ZONE).minusDays(1);

        generateSlots(pastDate, pastDate, LocalTime.of(9, 0), LocalTime.of(10, 0), 60)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SLOT_GENERATION_INVALID"));

        assertThat(slotRepository.count()).isZero();
    }

    @Test
    void endDateBeforeStartDateShouldReturnBadRequestWithoutPersistingSlots()
            throws Exception {
        LocalDate startDate = futureBusinessDate();

        generateSlots(
                startDate,
                startDate.minusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                60
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertThat(slotRepository.count()).isZero();
    }

    private ResultActions generateSlots(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime dayStart,
            LocalTime dayEnd,
            int durationMinutes
    ) throws Exception {
        return mockMvc.perform(post("/api/resources/{id}/slots/generate", resource.getId())
                .cookie(adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "startDate", startDate,
                        "endDate", endDate,
                        "dayStartTime", dayStart,
                        "dayEndTime", dayEnd,
                        "slotDurationMinutes", durationMinutes
                ))));
    }

    private void createExistingSlot(Instant start, Instant end) {
        Slot slot = new Slot();
        slot.setResource(resource);
        slot.setSlotStart(start);
        slot.setSlotEnd(end);
        slotRepository.saveAndFlush(slot);
    }

    private List<Slot> savedSlots() {
        return slotRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Slot::getSlotStart))
                .toList();
    }

    private LocalDate futureBusinessDate() {
        return LocalDate.now(BUSINESS_TIME_ZONE).plusDays(1);
    }

    private Instant instant(LocalDate date, int hour, int minute) {
        return date.atTime(hour, minute).atZone(BUSINESS_TIME_ZONE).toInstant();
    }
}
