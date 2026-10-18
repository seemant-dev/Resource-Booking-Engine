package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.response.BookingPageResponse;
import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.service.BookingService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/me")
    public BookingPageResponse getMyBookings(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be zero or greater") int page
    ) {
        return bookingService.getMyBookings(page);
    }

    @DeleteMapping("/{id}")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}