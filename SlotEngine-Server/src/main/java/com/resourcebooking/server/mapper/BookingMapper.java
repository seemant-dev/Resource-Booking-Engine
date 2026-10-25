package com.resourcebooking.server.mapper;

import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.dto.response.AdminBookingResponse;
import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        Slot slot = booking.getSlot();
        Resource resource = slot.getResource();

        return new BookingResponse(
                booking.getId(),
                slot.getId(),
                slot.getSlotStart(),
                slot.getSlotEnd(),
                resource.getId(),
                resource.getName(),
                booking.getStatus(),
                booking.getBookedAt()
        );
    }

    public AdminBookingResponse toAdminResponse(Booking booking) {
        Slot slot = booking.getSlot();
        Resource resource = slot.getResource();

        return new AdminBookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getEmail(),
                slot.getId(),
                slot.getSlotStart(),
                slot.getSlotEnd(),
                resource.getId(),
                resource.getName(),
                booking.getStatus(),
                booking.getBookedAt(),
                booking.getCancelledAt()
        );
    }
}