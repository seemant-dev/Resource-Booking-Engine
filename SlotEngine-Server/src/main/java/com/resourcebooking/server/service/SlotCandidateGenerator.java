package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class SlotCandidateGenerator {

    public List<SlotWindow> generate(SlotGenerateRequest request, ZoneId businessTimeZone) {
        List<SlotWindow> candidates = new ArrayList<>();
        Duration slotDuration = Duration.ofMinutes(request.getSlotDurationMinutes());

        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            Instant currentStart = currentDate
                    .atTime(request.getDayStartTime())
                    .atZone(businessTimeZone)
                    .toInstant();
            Instant dayEnd = currentDate
                    .atTime(request.getDayEndTime())
                    .atZone(businessTimeZone)
                    .toInstant();

            while (!currentStart.plus(slotDuration).isAfter(dayEnd)) {
                Instant currentEnd = currentStart.plus(slotDuration);
                candidates.add(new SlotWindow(currentStart, currentEnd));
                currentStart = currentEnd;
            }

            currentDate = currentDate.plusDays(1);
        }

        return List.copyOf(candidates);
    }
}
