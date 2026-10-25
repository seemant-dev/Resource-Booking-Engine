package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.response.AdminBookingResponse;
import com.resourcebooking.server.dto.response.AdminStatsResponse;
import com.resourcebooking.server.dto.response.PageResponse;
import com.resourcebooking.server.mapper.BookingMapper;
import com.resourcebooking.server.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Duration EXPIRING_WINDOW = Duration.ofHours(24);

    private final AdminRepository adminRepository;
    private final BookingMapper bookingMapper;
    private final Clock clock;
    private final ZoneId businessTimeZone;

    @Transactional(readOnly = true)
    public PageResponse<AdminBookingResponse> getBookings(String userEmail, String resourceName, int page, int size) {
        String emailFilter = normalizeFilter(userEmail);
        String resourceFilter = normalizeFilter(resourceName);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("bookedAt"), Sort.Order.desc("id"))
        );

        Page<AdminBookingResponse> bookings;
        if (emailFilter != null && resourceFilter != null) {
            bookings = adminRepository.findByUserEmailContainingIgnoreCaseAndSlotResourceNameContainingIgnoreCase(
                            emailFilter, resourceFilter, pageable)
                    .map(bookingMapper::toAdminResponse);
        } else if (emailFilter != null) {
            bookings = adminRepository.findByUserEmailContainingIgnoreCase(emailFilter, pageable)
                    .map(bookingMapper::toAdminResponse);
        } else if (resourceFilter != null) {
            bookings = adminRepository.findBySlotResourceNameContainingIgnoreCase(resourceFilter, pageable)
                    .map(bookingMapper::toAdminResponse);
        } else {
            bookings = adminRepository.findAllForAdmin(pageable)
                    .map(bookingMapper::toAdminResponse);
        }

        return new PageResponse<>(
                bookings.getContent(),
                bookings.getNumber(),
                bookings.getSize(),
                bookings.getTotalElements(),
                bookings.getTotalPages(),
                bookings.hasNext(),
                bookings.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        Instant now = clock.instant();
        LocalDate today = now.atZone(businessTimeZone).toLocalDate();
        Instant todayStart = today.atStartOfDay(businessTimeZone).toInstant();

        AdminRepository.AdminStatsCounts counts = adminRepository.getStats(
                OffsetDateTime.ofInstant(todayStart, ZoneOffset.UTC),
                OffsetDateTime.ofInstant(now, ZoneOffset.UTC),
                OffsetDateTime.ofInstant(now.plus(EXPIRING_WINDOW), ZoneOffset.UTC)
        );

        return new AdminStatsResponse(
                counts.getTotalResources(),
                counts.getBookingsToday(),
                counts.getSlotsExpiringUnbooked()
        );
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
