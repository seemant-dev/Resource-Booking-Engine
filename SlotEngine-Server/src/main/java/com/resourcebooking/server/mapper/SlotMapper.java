package com.resourcebooking.server.mapper;

import com.resourcebooking.server.dto.response.SlotResponse;
import com.resourcebooking.server.entity.Slot;
import org.springframework.stereotype.Component;

@Component
public class SlotMapper {

    public SlotResponse toResponse(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getSlotStart(),
                slot.getSlotEnd(),
                slot.isBooked()
        );
    }
}