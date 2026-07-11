package com.resourcebooking.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SlotResponse {

    private Long id;
    private Instant slotStart;
    private Instant slotEnd;
    private boolean booked;
}