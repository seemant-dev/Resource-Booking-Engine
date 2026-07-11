package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.response.SlotResponse;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.exception.ResourceNotFoundException;
import com.resourcebooking.server.mapper.SlotMapper;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import com.resourcebooking.server.dto.response.SlotGenerateResponse;
import com.resourcebooking.server.entity.Slot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final ResourceRepository resourceRepository;
    private final SlotMapper slotMapper;

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsForResourceOnDate(Long resourceId, LocalDate date) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        var startInclusive = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        var endExclusive = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

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
    public SlotGenerateResponse generateSlots(Long resourceId, SlotGenerateRequest request) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        int generated = 0;
        int skipped = 0;

        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            Instant currentStart = currentDate
                    .atTime(request.getDayStartTime())
                    .toInstant(ZoneOffset.UTC);

            Instant dayEnd = currentDate
                    .atTime(request.getDayEndTime())
                    .toInstant(ZoneOffset.UTC);

            Duration slotDuration = Duration.ofMinutes(request.getSlotDurationMinutes());

            while (!currentStart.plus(slotDuration).isAfter(dayEnd)) {
                Instant currentEnd = currentStart.plus(slotDuration);

                boolean overlapsExistingSlot = slotRepository
                        .existsByResourceIdAndSlotStartBeforeAndSlotEndAfter(
                                resource.getId(),
                                currentEnd,
                                currentStart
                        );

                if (overlapsExistingSlot) {
                    skipped++;
                } else {
                    Slot slot = new Slot();
                    slot.setResource(resource);
                    slot.setSlotStart(currentStart);
                    slot.setSlotEnd(currentEnd);
                    slot.setBooked(false);

                    slotRepository.save(slot);
                    generated++;
                }

                currentStart = currentEnd;
            }

            currentDate = currentDate.plusDays(1);
        }

        return new SlotGenerateResponse(generated, skipped);
    }
}