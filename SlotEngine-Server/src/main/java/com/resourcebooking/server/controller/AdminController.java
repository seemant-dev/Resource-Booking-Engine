package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.request.AdminBookingSearchRequest;
import com.resourcebooking.server.dto.response.AdminBookingResponse;
import com.resourcebooking.server.dto.response.AdminStatsResponse;
import com.resourcebooking.server.dto.response.PageResponse;
import com.resourcebooking.server.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/bookings")
    public PageResponse<AdminBookingResponse> getBookings(
            @Valid @RequestBody AdminBookingSearchRequest request,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return adminService.getBookings(
                request.getUserEmail(),
                request.getResourceName(),
                page,
                size
        );
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getStats();
    }
}
