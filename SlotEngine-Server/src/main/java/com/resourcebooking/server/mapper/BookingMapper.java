package com.resourcebooking.server.mapper;

import com.resourcebooking.server.dto.response.BookingResponse;
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
}