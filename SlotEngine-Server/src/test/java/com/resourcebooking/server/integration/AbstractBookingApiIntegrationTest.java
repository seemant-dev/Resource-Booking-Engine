package com.resourcebooking.server.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.Cookie;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractBookingApiIntegrationTest extends AbstractPostgresIntegrationTest {

    protected static final String PASSWORD = "Password@123";

    private static final Instant FIRST_SLOT_START = Instant.parse("2030-01-01T00:00:00Z");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected ResourceRepository resourceRepository;

    @Autowired
    protected SlotRepository slotRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private int nextSlotIndex;

    protected User createUser(String label, Role role) {
        User user = new User();
        user.setName(label);
        user.setEmail(label.toLowerCase().replace(' ', '.') + "-" + System.nanoTime() + "@example.com");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    protected Resource createResource(String name) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setDescription("Created by booking API integration tests");
        return resourceRepository.saveAndFlush(resource);
    }

    protected List<Booking> createBookings(User user, Resource resource, int count) {
        List<Slot> slots = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Instant start = FIRST_SLOT_START.plus(nextSlotIndex++, ChronoUnit.HOURS);
            Slot slot = new Slot();
            slot.setResource(resource);
            slot.setSlotStart(start);
            slot.setSlotEnd(start.plus(30, ChronoUnit.MINUTES));
            slot.setBooked(true);
            slots.add(slot);
        }

        List<Slot> savedSlots = slotRepository.saveAllAndFlush(slots);
        List<Booking> bookings = savedSlots.stream()
                .map(slot -> {
                    Booking booking = new Booking();
                    booking.setUser(user);
                    booking.setSlot(slot);
                    booking.setStatus(BookingStatus.CONFIRMED);
                    return booking;
                })
                .toList();

        return bookingRepository.saveAllAndFlush(bookings);
    }

    protected Slot createSlot(Resource resource, Instant start, boolean booked) {
        Slot slot = new Slot();
        slot.setResource(resource);
        slot.setSlotStart(start);
        slot.setSlotEnd(start.plus(30, ChronoUnit.MINUTES));
        slot.setBooked(booked);
        return slotRepository.saveAndFlush(slot);
    }

    protected void setBookingTimestamp(Collection<Booking> bookings, Instant timestamp) {
        bookings.forEach(booking -> jdbcTemplate.update(
                "UPDATE bookings SET booked_at = ? WHERE id = ?",
                OffsetDateTime.ofInstant(timestamp, ZoneOffset.UTC),
                booking.getId()
        ));
    }

    protected Cookie login(User user) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", user.getEmail(),
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("access_token");
        assertThat(accessToken).isNotNull();
        return accessToken;
    }

    protected JsonNode responseBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected List<Long> responseBookingIds(MvcResult result) throws Exception {
        List<Long> ids = new ArrayList<>();
        responseBody(result).path("content").forEach(item -> ids.add(item.path("id").asLong()));
        return ids;
    }

    protected Statistics hibernateStatistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
