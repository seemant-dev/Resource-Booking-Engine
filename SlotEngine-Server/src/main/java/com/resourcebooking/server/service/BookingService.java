package com.resourcebooking.server.service;

import com.resourcebooking.server.config.properties.BookingProperties;
import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.enums.LockingStrategy;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.exception.BookingNotFoundException;
import com.resourcebooking.server.exception.SlotAlreadyBookedException;
import com.resourcebooking.server.exception.SlotNotFoundException;
import com.resourcebooking.server.mapper.BookingMapper;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.repository.UserRepository;
import com.resourcebooking.server.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;
    private final SecurityUtils securityUtils;

    @Transactional
    public BookingResponse bookSlot(Long slotId) {
        Slot slot = findSlotForBooking(slotId);

        if (slot.isBooked()) {
            throw new SlotAlreadyBookedException(slotId);
        }

        Long currentUserId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        slot.setBooked(true);

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        Long currentUserId = securityUtils.getCurrentUserId();

        return bookingRepository.findByUserIdOrderByBookedAtDesc(currentUserId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        assertCanCancelBooking(booking);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return bookingMapper.toResponse(booking);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.getSlot().setBooked(false);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    private void assertCanCancelBooking(Booking booking) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Role currentUserRole = securityUtils.getCurrentUserRole();

        boolean ownsBooking = booking.getUser().getId().equals(currentUserId);
        boolean isAdmin = currentUserRole == Role.ADMIN;

        if (!ownsBooking && !isAdmin) {
            throw new AccessDeniedException("You can cancel only your own bookings");
        }
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