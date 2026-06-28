package com.resourcebooking.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
}