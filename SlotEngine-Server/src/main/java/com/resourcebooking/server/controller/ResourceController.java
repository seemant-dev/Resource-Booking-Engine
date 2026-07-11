package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.request.ResourceRequest;
import com.resourcebooking.server.dto.response.ResourceResponse;
import com.resourcebooking.server.service.ResourceService;
import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import com.resourcebooking.server.dto.response.SlotGenerateResponse;
import com.resourcebooking.server.dto.response.SlotResponse;
import com.resourcebooking.server.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final SlotService slotService;

    @GetMapping
    public List<ResourceResponse> getAllResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/{id}")
    public ResourceResponse getResourceById(@PathVariable Long id) {
        return resourceService.getResourceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createResource(@Valid @RequestBody ResourceRequest request) {
        return resourceService.createResource(request);
    }

    @PutMapping("/{id}")
    public ResourceResponse updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request
    ) {
        return resourceService.updateResource(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
    }

    @GetMapping("/{id}/slots")
    public List<SlotResponse> getSlotsForResourceOnDate(
            @PathVariable Long id,
            @RequestParam LocalDate date
    ) {
        return slotService.getSlotsForResourceOnDate(id, date);
    }

    @PostMapping("/{id}/slots/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotGenerateResponse generateSlots(
            @PathVariable Long id,
            @Valid @RequestBody SlotGenerateRequest request
    ) {
        return slotService.generateSlots(id, request);
    }
}