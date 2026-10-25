package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.request.SlotGenerateRequest;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.mapper.SlotMapper;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SlotServiceTimeZoneTest {

    private final SlotRepository slotRepository = mock(SlotRepository.class);
    private final ResourceRepository resourceRepository = mock(ResourceRepository.class);
    private final SlotService slotService = new SlotService(
            slotRepository,
            resourceRepository,
            mock(SlotMapper.class),
            ZoneId.of("Asia/Kolkata")
    );

    @Test
    void generatesNineAmBusinessTimeAsThreeThirtyUtc() {
        Resource resource = mock(Resource.class);
        when(resource.getId()).thenReturn(1L);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        SlotGenerateRequest request = new SlotGenerateRequest();
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setDayStartTime(LocalTime.of(9, 0));
        request.setDayEndTime(LocalTime.of(10, 0));
        request.setSlotDurationMinutes(60);

        slotService.generateSlots(1L, request);

        ArgumentCaptor<Slot> slot = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(slot.capture());
        assertThat(slot.getValue().getSlotStart()).isEqualTo(Instant.parse("2026-01-01T03:30:00Z"));
        assertThat(slot.getValue().getSlotEnd()).isEqualTo(Instant.parse("2026-01-01T04:30:00Z"));
    }

    @Test
    void queriesTheBusinessDateUsingItsInstantBoundaries() {
        Resource resource = mock(Resource.class);
        when(resource.getId()).thenReturn(1L);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        slotService.getSlotsForResourceOnDate(1L, LocalDate.of(2026, 1, 1));

        verify(slotRepository).findByResourceIdAndSlotStartBetween(
                eq(1L),
                eq(Instant.parse("2025-12-31T18:30:00Z")),
                eq(Instant.parse("2026-01-01T18:30:00Z"))
        );
    }
}
