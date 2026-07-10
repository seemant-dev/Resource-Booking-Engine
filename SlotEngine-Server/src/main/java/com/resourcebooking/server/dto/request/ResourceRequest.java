package com.resourcebooking.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    @Size(max = 150, message = "Resource name must be at most 150 characters")
    private String name;

    @Size(max = 500, message = "Resource description must be at most 500 characters")
    private String description;
}
