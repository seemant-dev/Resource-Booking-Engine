package com.resourcebooking.server.integration.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.Slot;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.BookingStatus;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.integration.AbstractPostgresIntegrationTest;
import com.resourcebooking.server.repository.BookingRepository;
import com.resourcebooking.server.repository.ResourceRepository;
import com.resourcebooking.server.repository.SlotRepository;
import com.resourcebooking.server.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingHttpIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void bookingAvailableSlotShouldReturnCreatedAndPersistConfirmedBooking() throws Exception {
        User user = createUser("booking-success", Role.USER);
        Slot slot = createAvailableSlot();
        Cookie accessToken = login(user);

        mockMvc.perform(post("/api/slots/{id}/book", slot.getId())
                        .cookie(accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.slotId").value(slot.getId().intValue()))
                .andExpect(jsonPath("$.resourceId").value(slot.getResource().getId().intValue()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).singleElement().satisfies(booking -> {
            assertThat(booking.getSlot().getId()).isEqualTo(slot.getId());
            assertThat(booking.getUser().getId()).isEqualTo(user.getId());
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        });
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isTrue();
    }

    @Test
    void bookingWithoutAccessTokenShouldReturnUnauthorizedAndLeaveSlotAvailable() throws Exception {
        Slot slot = createAvailableSlot();

        mockMvc.perform(post("/api/slots/{id}/book", slot.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        assertThat(bookingRepository.count()).isZero();
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isFalse();
    }

    @Test
    void bookingAlreadyBookedSlotShouldReturnConflictAndPreserveOriginalBooking() throws Exception {
        User firstUser = createUser("original-booker", Role.USER);
        User secondUser = createUser("conflicting-booker", Role.USER);
        Slot slot = createAvailableSlot();

        long originalBookingId = bookSlot(slot.getId(), login(firstUser));

        mockMvc.perform(post("/api/slots/{id}/book", slot.getId())
                        .cookie(login(secondUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SLOT_ALREADY_BOOKED"));

        assertThat(bookingRepository.countBySlotIdAndStatus(
                slot.getId(),
                BookingStatus.CONFIRMED
        )).isEqualTo(1);
        assertThat(bookingRepository.findById(originalBookingId).orElseThrow().getUser().getId())
                .isEqualTo(firstUser.getId());
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isTrue();
    }

    @Test
    void ownerCancellationShouldFreeSlotAndAllowAnotherUserToRebook() throws Exception {
        User owner = createUser("cancellation-owner", Role.USER);
        User nextUser = createUser("next-booker", Role.USER);
        Slot slot = createAvailableSlot();
        Cookie ownerAccessToken = login(owner);
        long cancelledBookingId = bookSlot(slot.getId(), ownerAccessToken);

        mockMvc.perform(delete("/api/bookings/{id}", cancelledBookingId)
                        .cookie(ownerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) cancelledBookingId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Booking cancelledBooking = bookingRepository.findById(cancelledBookingId).orElseThrow();
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelledBooking.getCancelledAt()).isNotNull();
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isFalse();

        long replacementBookingId = bookSlot(slot.getId(), login(nextUser));

        assertThat(replacementBookingId).isNotEqualTo(cancelledBookingId);
        assertThat(bookingRepository.countBySlotIdAndStatus(
                slot.getId(),
                BookingStatus.CONFIRMED
        )).isEqualTo(1);
        assertThat(bookingRepository.findById(replacementBookingId).orElseThrow().getUser().getId())
                .isEqualTo(nextUser.getId());
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isTrue();
    }

    @Test
    void ownerCancellingTwiceShouldRemainSuccessfulAndNotChangeCancellationTime() throws Exception {
        User owner = createUser("repeat-cancellation-owner", Role.USER);
        Slot slot = createAvailableSlot();
        Cookie accessToken = login(owner);
        long bookingId = bookSlot(slot.getId(), accessToken);

        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
                        .cookie(accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Instant firstCancellationTime = bookingRepository.findById(bookingId)
                .orElseThrow()
                .getCancelledAt();

        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
                        .cookie(accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getCancelledAt()).isEqualTo(firstCancellationTime);
        assertThat(bookingRepository.count()).isEqualTo(1);
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isFalse();
    }

    @Test
    void adminShouldCancelAnotherUsersBooking() throws Exception {
        User owner = createUser("admin-cancellation-owner", Role.USER);
        User admin = createUser("cancelling-admin", Role.ADMIN);
        Slot slot = createAvailableSlot();
        long bookingId = bookSlot(slot.getId(), login(owner));

        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
                        .cookie(login(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelledAt()).isNotNull();
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isFalse();
    }

    @Test
    void nonOwnerShouldRemainForbiddenWhenBookingIsAlreadyCancelled() throws Exception {
        User owner = createUser("forbidden-cancellation-owner", Role.USER);
        User nonOwner = createUser("forbidden-cancellation-user", Role.USER);
        Slot slot = createAvailableSlot();
        Cookie ownerAccessToken = login(owner);
        long bookingId = bookSlot(slot.getId(), ownerAccessToken);

        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
                        .cookie(ownerAccessToken))
                .andExpect(status().isOk());

        Instant originalCancellationTime = bookingRepository.findById(bookingId)
                .orElseThrow()
                .getCancelledAt();

        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
                        .cookie(login(nonOwner)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelledAt()).isEqualTo(originalCancellationTime);
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isFalse();
    }

    private User createUser(String emailPrefix, Role role) {
        User user = new User();
        user.setName(emailPrefix);
        user.setEmail(emailPrefix + "-" + System.nanoTime() + "@example.com");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private Slot createAvailableSlot() {
        Resource resource = new Resource();
        resource.setName("Booking HTTP test resource " + System.nanoTime());
        resource.setDescription("Created by booking HTTP integration test");
        Resource savedResource = resourceRepository.saveAndFlush(resource);

        Slot slot = new Slot();
        slot.setResource(savedResource);
        slot.setSlotStart(Instant.now().plusSeconds(86_400));
        slot.setSlotEnd(Instant.now().plusSeconds(88_200));
        return slotRepository.saveAndFlush(slot);
    }

    private Cookie login(User user) throws Exception {
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

    private long bookSlot(Long slotId, Cookie accessToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/slots/{id}/book", slotId)
                        .cookie(accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("id")
                .asLong();
    }
}
