package com.resourcebooking.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStatsResponse {

    private long totalResources;
    private long bookingsToday;
    private long slotsExpiringUnbooked;
}
