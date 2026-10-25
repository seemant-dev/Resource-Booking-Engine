package com.resourcebooking.server.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminBookingSearchRequest {

    @Size(max = 255)
    private String userEmail;

    @Size(max = 150)
    private String resourceName;
}
