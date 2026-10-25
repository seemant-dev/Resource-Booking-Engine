package com.resourcebooking.server.dto.response;

import com.resourcebooking.server.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AdminBookingResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private Long slotId;
    private Instant slotStart;
    private Instant slotEnd;
    private Long resourceId;
    private String resourceName;
    private BookingStatus status;
    private Instant bookedAt;
    private Instant cancelledAt;
}
