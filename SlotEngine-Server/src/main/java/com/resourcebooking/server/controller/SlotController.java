package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.response.BookingResponse;
import com.resourcebooking.server.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
public class SlotController {

    private final BookingService bookingService;

    @PostMapping("/{id}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookSlot(@PathVariable Long id) {
        return bookingService.bookSlot(id);
    }
}