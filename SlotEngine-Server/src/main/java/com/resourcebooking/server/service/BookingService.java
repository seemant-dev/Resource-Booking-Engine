package com.resourcebooking.server.service;

import com.resourcebooking.server.config.properties.BookingProperties;
import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.enums.LockingStrategy;
import com.resourcebooking.server.exception.SlotAlreadyBookedException;
import com.resourcebooking.server.exception.SlotNotFoundException;
import com.resourcebooking.server.mapper.BookingMapper;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Long PHASE_TWO_USER_ID = 1L;

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;

    @Transactional
    public BookingResponse bookSlot(Long slotId) {
        Slot slot = findSlotForBooking(slotId);

        if (slot.isBooked()) {
            throw new SlotAlreadyBookedException(slotId);
        }

        User user = userRepository.findById(PHASE_TWO_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Phase 2 seed user not found"));

        slot.setBooked(true);

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    private Slot findSlotForBooking(Long slotId) {
        if (bookingProperties.getLockingStrategy() == LockingStrategy.PESSIMISTIC) {
            return slotRepository.findByIdWithLock(slotId)
                    .orElseThrow(() -> new SlotNotFoundException(slotId));
        }

        return slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(slotId));
    }
}