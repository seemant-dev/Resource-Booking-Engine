package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/me")
    public List<BookingResponse> getMyBookings() {
        return bookingService.getMyBookings();
    }

    @DeleteMapping("/{id}")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}