package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.response.ResourceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @GetMapping("/{id}")
    public ResourceResponse getResourceById(@PathVariable Long id) {
        return new ResourceResponse(id, "Room A", "desc");
    }
}
