package com.resourcebooking.server.integration.booking;

import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.exception.SlotAlreadyBookedException;
import com.resourcebooking.server.integration.AbstractPostgresIntegrationTest;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.repository.UserRepository;
import com.resourcebooking.server.service.BookingService;
import com.resourcebooking.server.security.UserPrincipal;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractBookingConcurrencyIntegrationTest extends AbstractPostgresIntegrationTest {

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

    @Autowired
    private UserRepository userRepository;

    protected void verifyOnlyOneBookingSucceedsUnderConcurrentLoad(String strategyName) throws Exception {
        TestUser bookingUser = createBookingUser(strategyName);
        Slot slot = createAvailableSlot(strategyName);

        ExecutorService executorService = Executors.newFixedThreadPool(ATTEMPT_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(ATTEMPT_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<BookingAttemptResult>> futures = new ArrayList<>();

            for (int i = 0; i < ATTEMPT_COUNT; i++) {
                futures.add(executorService.submit(createBookingAttempt(
                        slot.getId(),
                        bookingUser,
                        readyLatch,
                        startLatch
                )));
            }

            readyLatch.await();
            startLatch.countDown();

            BookingAttemptCounts counts = collectAttemptCounts(futures);

            long recognizedConflictCount = counts.slotAlreadyBookedCount()
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
                              recognizedConflictCount = {}
                              slotAlreadyBookedCount = {}
                              optimisticLockConflictCount = {}
                              dataIntegrityConflictCount = {}
                              otherFailureCount = {}
                              confirmedBookingCount = {}
                              updatedSlot.isBooked = {}
                            """,
                    strategyName,
                    counts.successCount(),
                    recognizedConflictCount,
                    counts.slotAlreadyBookedCount(),
                    counts.optimisticLockConflictCount(),
                    counts.dataIntegrityConflictCount(),
                    counts.otherFailureCount(),
                    confirmedBookingCount,
                    updatedSlot.isBooked()
            );

            assertThat(counts.successCount()).isEqualTo(1);
            assertThat(recognizedConflictCount).isEqualTo(ATTEMPT_COUNT - 1);
            assertThat(counts.otherFailureCount()).isZero();
            assertThat(confirmedBookingCount).isEqualTo(1);
            assertThat(updatedSlot.isBooked()).isTrue();
        } finally {
            executorService.shutdownNow();
        }
    }

    protected void verifyAllDifferentSlotBookingsSucceedUnderConcurrentLoad(String strategyName) throws Exception {
        Resource resource = createResource(strategyName + " Independent Slots");
        List<BookingAssignment> assignments = createBookingAssignments(strategyName, resource);

        ExecutorService executorService = Executors.newFixedThreadPool(ATTEMPT_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(ATTEMPT_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<BookingAttemptResult>> futures = new ArrayList<>();

            for (BookingAssignment assignment : assignments) {
                futures.add(executorService.submit(createBookingAttempt(
                        assignment.slotId(),
                        assignment.user(),
                        readyLatch,
                        startLatch
                )));
            }

            readyLatch.await();
            startLatch.countDown();

            BookingAttemptCounts counts = collectAttemptCounts(futures);
            List<Booking> bookings = bookingRepository.findAll();
            Map<Long, Long> expectedUserBySlot = assignments.stream()
                    .collect(Collectors.toMap(
                            BookingAssignment::slotId,
                            assignment -> assignment.user().id()
                    ));

            assertThat(counts.successCount()).isEqualTo(ATTEMPT_COUNT);
            assertThat(counts.slotAlreadyBookedCount()).isZero();
            assertThat(counts.optimisticLockConflictCount()).isZero();
            assertThat(counts.dataIntegrityConflictCount()).isZero();
            assertThat(counts.otherFailureCount()).isZero();

            assertThat(bookings).hasSize(ATTEMPT_COUNT).allSatisfy(booking -> {
                assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
                assertThat(booking.getUser().getId())
                        .isEqualTo(expectedUserBySlot.get(booking.getSlot().getId()));
            });

            assertThat(slotRepository.findAllById(expectedUserBySlot.keySet()))
                    .hasSize(ATTEMPT_COUNT)
                    .allSatisfy(slot -> assertThat(slot.isBooked()).isTrue());
        } finally {
            startLatch.countDown();
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
            TestUser bookingUser,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) {
        return () -> {
            readyLatch.countDown();
            startLatch.await();

            authenticateAs(bookingUser);

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
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private void authenticateAs(TestUser user) {
        UserPrincipal principal = new UserPrincipal(
                user.id(),
                user.name(),
                user.email(),
                user.passwordHash(),
                user.role()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private TestUser createBookingUser(String strategyName) {
        return saveBookingUser(
                strategyName + " Concurrency User",
                strategyName + "-concurrency@example.com"
        );
    }

    private TestUser createBookingUser(String strategyName, int userNumber) {
        return saveBookingUser(
                strategyName + " Concurrency User " + userNumber,
                strategyName + "-concurrency-" + userNumber + "@example.com"
        );
    }

    private TestUser saveBookingUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("test-only-password-hash");
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return new TestUser(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPasswordHash(),
                savedUser.getRole()
        );
    }

    private List<BookingAssignment> createBookingAssignments(String strategyName, Resource resource) {
        Instant firstSlotStart = Instant.parse("2027-08-01T09:00:00Z");
        List<BookingAssignment> assignments = new ArrayList<>(ATTEMPT_COUNT);

        for (int i = 0; i < ATTEMPT_COUNT; i++) {
            TestUser user = createBookingUser(strategyName, i + 1);
            Instant slotStart = firstSlotStart.plusSeconds(i * 30L * 60L);

            Slot slot = new Slot();
            slot.setResource(resource);
            slot.setSlotStart(slotStart);
            slot.setSlotEnd(slotStart.plusSeconds(30L * 60L));
            slot.setBooked(false);

            Slot savedSlot = slotRepository.save(slot);
            assignments.add(new BookingAssignment(user, savedSlot.getId()));
        }

        return assignments;
    }

    private Resource createResource(String testName) {
        Resource resource = new Resource();
        resource.setName(testName + " Test Room " + System.nanoTime());
        resource.setDescription("Created by " + testName + " concurrency test");
        return resourceRepository.save(resource);
    }

    private Slot createAvailableSlot(String strategyName) {
        Resource resource = createResource(strategyName);

        Slot slot = new Slot();
        slot.setResource(resource);
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

    private record TestUser(
            Long id,
            String name,
            String email,
            String passwordHash,
            Role role
    ) {
    }

    private record BookingAssignment(TestUser user, Long slotId) {
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
