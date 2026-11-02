package com.resourcebooking.server.integration.slot;

import com.resourcebooking.server.common.DatabaseConstraints;
import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import com.resourcebooking.server.dto.response.SlotGenerateResponse;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.integration.AbstractPostgresIntegrationTest;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.service.SlotService;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SlotGenerationIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    private SlotService slotService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void insertsOnlyCandidatesThatDoNotOverlapExistingSlots() {
        Resource resource = createResource("Overlap filtering room");
        LocalDate date = futureBusinessDate();
        createSlot(resource, instant(date, 10, 0), instant(date, 11, 15));

        SlotGenerateResponse response = slotService.generateSlots(
                resource.getId(),
                request(date, LocalTime.of(9, 0), LocalTime.of(12, 0), 60)
        );

        assertThat(response.getGenerated()).isEqualTo(1);
        assertThat(response.getSkipped()).isEqualTo(2);
        assertThat(slotStarts(resource.getId())).containsExactly(
                instant(date, 9, 0),
                instant(date, 10, 0)
        );
    }

    @Test
    void batchInsertsAllGeneratedCandidates() {
        Resource resource = createResource("Batch insertion room");
        LocalDate date = futureBusinessDate();

        SlotGenerateResponse response = slotService.generateSlots(
                resource.getId(),
                request(date, LocalTime.MIDNIGHT, LocalTime.of(18, 45), 15)
        );

        assertThat(response.getGenerated()).isEqualTo(75);
        assertThat(response.getSkipped()).isZero();
        assertThat(slotCount(resource.getId())).isEqualTo(75);
    }

    @Test
    void concurrentGenerationForSameResourceCreatesOneSchedule() throws Exception {
        Resource resource = createResource("Concurrent generation room");
        LocalDate date = futureBusinessDate();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<SlotGenerateResponse> first = executor.submit(
                    () -> generateAfterSignal(resource.getId(), date, ready, start)
            );
            Future<SlotGenerateResponse> second = executor.submit(
                    () -> generateAfterSignal(resource.getId(), date, ready, start)
            );

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            SlotGenerateResponse firstResponse = first.get(10, TimeUnit.SECONDS);
            SlotGenerateResponse secondResponse = second.get(10, TimeUnit.SECONDS);

            assertThat(List.of(firstResponse.getGenerated(), secondResponse.getGenerated()))
                    .containsExactlyInAnyOrder(3, 0);
            assertThat(List.of(firstResponse.getSkipped(), secondResponse.getSkipped()))
                    .containsExactlyInAnyOrder(0, 3);
            assertThat(slotCount(resource.getId())).isEqualTo(3);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void gistConstraintAllowsBackToBackSlotsButRejectsOverlap() {
        Resource resource = createResource("Database constraint room");
        LocalDate date = futureBusinessDate();

        insertSlot(resource.getId(), instant(date, 9, 0), instant(date, 10, 0));
        insertSlot(resource.getId(), instant(date, 10, 0), instant(date, 11, 0));

        assertThatThrownBy(() -> insertSlot(
                resource.getId(),
                instant(date, 9, 30),
                instant(date, 10, 30)
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(exception -> {
                    assertThat(findSqlState(exception)).isEqualTo("23P01");
                    assertThat(findPostgresConstraintName(exception))
                            .isEqualTo(DatabaseConstraints.SLOTS_RESOURCE_TIME_EXCLUSION);
                });

        assertThat(slotCount(resource.getId())).isEqualTo(2);
    }

    private SlotGenerateResponse generateAfterSignal(
            Long resourceId,
            LocalDate date,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting to start concurrent generation");
        }

        return slotService.generateSlots(
                resourceId,
                request(date, LocalTime.of(9, 0), LocalTime.of(12, 0), 60)
        );
    }

    private Resource createResource(String name) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setDescription("Created by slot generation integration test");
        return resourceRepository.saveAndFlush(resource);
    }

    private void createSlot(Resource resource, Instant start, Instant end) {
        Slot slot = new Slot();
        slot.setResource(resource);
        slot.setSlotStart(start);
        slot.setSlotEnd(end);
        slotRepository.saveAndFlush(slot);
    }

    private SlotGenerateRequest request(
            LocalDate date,
            LocalTime start,
            LocalTime end,
            int durationMinutes
    ) {
        SlotGenerateRequest request = new SlotGenerateRequest();
        request.setStartDate(date);
        request.setEndDate(date);
        request.setDayStartTime(start);
        request.setDayEndTime(end);
        request.setSlotDurationMinutes(durationMinutes);
        return request;
    }

    private LocalDate futureBusinessDate() {
        return LocalDate.now(BUSINESS_TIME_ZONE).plusDays(1);
    }

    private Instant instant(LocalDate date, int hour, int minute) {
        return date.atTime(hour, minute).atZone(BUSINESS_TIME_ZONE).toInstant();
    }

    private void insertSlot(Long resourceId, Instant start, Instant end) {
        jdbcTemplate.update(
                "insert into slots (resource_id, slot_start, slot_end) values (?, ?, ?)",
                resourceId,
                Timestamp.from(start),
                Timestamp.from(end)
        );
    }

    private List<Instant> slotStarts(Long resourceId) {
        return jdbcTemplate.query(
                "select slot_start from slots where resource_id = ? order by slot_start",
                (resultSet, rowNumber) -> resultSet.getTimestamp("slot_start").toInstant(),
                resourceId
        );
    }

    private long slotCount(Long resourceId) {
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from slots where resource_id = ?",
                Long.class,
                resourceId
        );
        return count == null ? 0 : count;
    }

    private String findSqlState(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException.getSQLState();
            }
            current = current.getCause();
        }

        return null;
    }

    private String findPostgresConstraintName(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof PSQLException postgresException
                    && postgresException.getServerErrorMessage() != null) {
                return postgresException.getServerErrorMessage().getConstraint();
            }
            current = current.getCause();
        }

        return null;
    }
}
