package com.resourcebooking.server.integration.booking;

import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingConcurrencyTest {

    private static final int ATTEMPT_COUNT = 20;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void onlyOneBookingSucceedsWhenMultipleUsersBookSameSlotConcurrently() throws Exception {
        Slot slot = createAvailableSlot();

        ExecutorService executorService = Executors.newFixedThreadPool(ATTEMPT_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(ATTEMPT_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Callable<Boolean>> bookingAttempts = new ArrayList<>();

        for (int i = 0; i < ATTEMPT_COUNT; i++) {
            bookingAttempts.add(() -> {
                readyLatch.countDown();
                startLatch.await();

                try {
                    BookingResponse response = bookingService.bookSlot(slot.getId());
                    return response.getStatus() == BookingStatus.CONFIRMED;
                } catch (RuntimeException exception) {
                    return false;
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();

        long successCount = executorService.invokeAll(bookingAttempts)
                .stream()
                .filter(future -> {
                    try {
                        return future.get();
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .count();

        executorService.shutdown();

        long confirmedBookingCount = bookingRepository.countBySlotIdAndStatus(
                slot.getId(),
                BookingStatus.CONFIRMED
        );

        Slot updatedSlot = slotRepository.findById(slot.getId()).orElseThrow();

        assertThat(successCount).isEqualTo(1);
        assertThat(confirmedBookingCount).isEqualTo(1);
        assertThat(updatedSlot.isBooked()).isTrue();
    }

    private Slot createAvailableSlot() {
        Resource resource = new Resource();
        resource.setName("Concurrency Test Room " + System.nanoTime());
        resource.setDescription("Created by concurrency test");

        Resource savedResource = resourceRepository.save(resource);

        Slot slot = new Slot();
        slot.setResource(savedResource);
        slot.setSlotStart(Instant.parse("2026-08-01T10:00:00Z"));
        slot.setSlotEnd(Instant.parse("2026-08-01T10:30:00Z"));
        slot.setBooked(false);

        return slotRepository.save(slot);
    }
}
