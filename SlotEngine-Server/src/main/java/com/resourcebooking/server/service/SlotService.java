package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import com.resourcebooking.server.dto.response.SlotGenerateResponse;
import com.resourcebooking.server.dto.response.SlotResponse;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.exception.InvalidSlotGenerationRequestException;
import com.resourcebooking.server.exception.ResourceNotFoundException;
import com.resourcebooking.server.mapper.SlotMapper;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotBatchRepository;
import com.resourcebooking.server.repository.SlotRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final SlotBatchRepository slotBatchRepository;
    private final ResourceRepository resourceRepository;
    private final SlotMapper slotMapper;
    private final SlotCandidateGenerator slotCandidateGenerator;
    private final Clock clock;
    private final ZoneId businessTimeZone;

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsForResourceOnDate(Long resourceId, LocalDate date) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        var startInclusive = date.atStartOfDay(businessTimeZone).toInstant();
        var endExclusive = date.plusDays(1).atStartOfDay(businessTimeZone).toInstant();

        return slotRepository.findByResourceIdAndSlotStartBetween(
                        resource.getId(),
                        startInclusive,
                        endExclusive
                )
                .stream()
                .map(slotMapper::toResponse)
                .toList();
    }

    @Transactional
    public SlotGenerateResponse generateSlots(Long resourceId, @Valid SlotGenerateRequest request) {
        validateStartDate(request);

        List<SlotWindow> candidates = slotCandidateGenerator.generate(request, businessTimeZone);
        if (candidates.isEmpty()) {
            throw new InvalidSlotGenerationRequestException(
                    "Generation request must produce at least one slot."
            );
        }

        Resource resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        List<Slot> existingSlots = slotRepository.findOverlappingRange(
                resource.getId(),
                candidates.getFirst().start(),
                candidates.getLast().end()
        );

        List<SlotWindow> availableCandidates = findAvailableCandidates(candidates, existingSlots);
        int skipped = candidates.size() - availableCandidates.size();

        if (!availableCandidates.isEmpty()) {
            slotBatchRepository.insertAll(resource.getId(), availableCandidates);
        }

        log.info(
                "Slots generated resourceId={} startDate={} endDate={} generated={} skipped={}",
                resourceId,
                request.getStartDate(),
                request.getEndDate(),
                availableCandidates.size(),
                skipped
        );

        return new SlotGenerateResponse(availableCandidates.size(), skipped);
    }

    private void validateStartDate(SlotGenerateRequest request) {
        LocalDate businessToday = LocalDate.now(clock.withZone(businessTimeZone));

        if (request.getStartDate().isBefore(businessToday)) {
            throw new InvalidSlotGenerationRequestException(
                    "Start date must be today or later in the business time zone."
            );
        }
    }

    private List<SlotWindow> findAvailableCandidates(
            List<SlotWindow> candidates,
            List<Slot> existingSlots
    ) {
        List<SlotWindow> available = new ArrayList<>(candidates.size());
        int existingIndex = 0;

        for (SlotWindow candidate : candidates) {
            while (existingIndex < existingSlots.size()
                    && !existingSlots.get(existingIndex).getSlotEnd().isAfter(candidate.start())) {
                existingIndex++;
            }

            boolean overlaps = existingIndex < existingSlots.size()
                    && existingSlots.get(existingIndex).getSlotStart().isBefore(candidate.end())
                    && existingSlots.get(existingIndex).getSlotEnd().isAfter(candidate.start());

            if (!overlaps) {
                available.add(candidate);
            }
        }

        return available;
    }

}
