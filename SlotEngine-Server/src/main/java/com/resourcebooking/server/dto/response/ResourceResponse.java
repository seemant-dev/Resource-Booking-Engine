package com.resourcebooking.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private boolean hasOpenSlots;
    private Instant createdAt;
}