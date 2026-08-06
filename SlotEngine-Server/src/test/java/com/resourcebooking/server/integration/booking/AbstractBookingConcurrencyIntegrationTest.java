package com.resourcebooking.server.integration.booking;

import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.exception.SlotAlreadyBookedException;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.service.BookingService;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractBookingConcurrencyIntegrationTest {

    private static final int ATTEMPT_COUNT = 20;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    protected void verifyOnlyOneBookingSucceedsUnderConcurrentLoad(String strategyName) throws Exception {
        Slot slot = createAvailableSlot(strategyName);

        ExecutorService executorService = Executors.newFixedThreadPool(ATTEMPT_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(ATTEMPT_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<BookingAttemptResult>> futures = new ArrayList<>();

            for (int i = 0; i < ATTEMPT_COUNT; i++) {
                futures.add(executorService.submit(createBookingAttempt(slot.getId(), readyLatch, startLatch)));
            }

            readyLatch.await();
            startLatch.countDown();

            BookingAttemptCounts counts = collectAttemptCounts(futures);

            long expectedFailureCount = counts.slotAlreadyBookedCount()
                    + counts.optimisticLockConflictCount()
                    + counts.dataIntegrityConflictCount();

            long confirmedBookingCount = bookingRepository.countBySlotIdAndStatus(
                    slot.getId(),
                    BookingStatus.CONFIRMED
            );

            Slot updatedSlot = slotRepository.findById(slot.getId()).orElseThrow();

            log.info(
                    """
                            
                            Booking concurrency result [{}]:
                              successCount = {}
                              expectedFailureCount = {}
                              slotAlreadyBookedCount = {}
                              optimisticLockConflictCount = {}
                              dataIntegrityConflictCount = {}
                              otherFailureCount = {}
                              confirmedBookingCount = {}
                              updatedSlot.isBooked = {}
                            """,
                    strategyName,
                    counts.successCount(),
                    expectedFailureCount,
                    counts.slotAlreadyBookedCount(),
                    counts.optimisticLockConflictCount(),
                    counts.dataIntegrityConflictCount(),
                    counts.otherFailureCount(),
                    confirmedBookingCount,
                    updatedSlot.isBooked()
            );

            assertThat(counts.successCount()).isEqualTo(1);
            assertThat(expectedFailureCount).isEqualTo(ATTEMPT_COUNT - 1);
            assertThat(counts.otherFailureCount()).isZero();
            assertThat(confirmedBookingCount).isEqualTo(1);
            assertThat(updatedSlot.isBooked()).isTrue();
        } finally {
            executorService.shutdownNow();
        }
    }

    private BookingAttemptCounts collectAttemptCounts(List<Future<BookingAttemptResult>> futures) throws Exception {
        long successCount = 0;
        long slotAlreadyBookedCount = 0;
        long optimisticLockConflictCount = 0;
        long dataIntegrityConflictCount = 0;
        long otherFailureCount = 0;

        for (Future<BookingAttemptResult> future : futures) {
            BookingAttemptResult result = future.get();

            switch (result) {
                case SUCCESS -> successCount++;
                case SLOT_ALREADY_BOOKED -> slotAlreadyBookedCount++;
                case OPTIMISTIC_LOCK_CONFLICT -> optimisticLockConflictCount++;
                case DATA_INTEGRITY_CONFLICT -> dataIntegrityConflictCount++;
                case OTHER_FAILURE -> otherFailureCount++;
            }
        }

        return new BookingAttemptCounts(
                successCount,
                slotAlreadyBookedCount,
                optimisticLockConflictCount,
                dataIntegrityConflictCount,
                otherFailureCount
        );
    }

    private Callable<BookingAttemptResult> createBookingAttempt(
            Long slotId,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) {
        return () -> {
            readyLatch.countDown();
            startLatch.await();

            try {
                BookingResponse response = bookingService.bookSlot(slotId);
                return response.getStatus() == BookingStatus.CONFIRMED
                        ? BookingAttemptResult.SUCCESS
                        : BookingAttemptResult.OTHER_FAILURE;
            } catch (SlotAlreadyBookedException exception) {
                return BookingAttemptResult.SLOT_ALREADY_BOOKED;
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException exception) {
                return BookingAttemptResult.OPTIMISTIC_LOCK_CONFLICT;
            } catch (DataIntegrityViolationException exception) {
                return BookingAttemptResult.DATA_INTEGRITY_CONFLICT;
            } catch (RuntimeException exception) {
                return BookingAttemptResult.OTHER_FAILURE;
            }
        };
    }

    private Slot createAvailableSlot(String strategyName) {
        Resource resource = new Resource();
        resource.setName(strategyName + " Concurrency Test Room " + System.nanoTime());
        resource.setDescription("Created by " + strategyName + " concurrency test");

        Resource savedResource = resourceRepository.save(resource);

        Slot slot = new Slot();
        slot.setResource(savedResource);
        slot.setSlotStart(Instant.parse("2026-08-01T10:00:00Z"));
        slot.setSlotEnd(Instant.parse("2026-08-01T10:30:00Z"));
        slot.setBooked(false);

        return slotRepository.save(slot);
    }

    private enum BookingAttemptResult {
        SUCCESS,
        SLOT_ALREADY_BOOKED,
        OPTIMISTIC_LOCK_CONFLICT,
        DATA_INTEGRITY_CONFLICT,
        OTHER_FAILURE
    }

    private record BookingAttemptCounts(
            long successCount,
            long slotAlreadyBookedCount,
            long optimisticLockConflictCount,
            long dataIntegrityConflictCount,
            long otherFailureCount
    ) {
    }
}