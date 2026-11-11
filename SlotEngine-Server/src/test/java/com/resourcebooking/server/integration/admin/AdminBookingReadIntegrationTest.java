package com.resourcebooking.server.integration.admin;

import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.integration.AbstractBookingApiIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@AutoConfigureMockMvc
@Import(AdminBookingReadIntegrationTest.FixedClockConfiguration.class)
class AdminBookingReadIntegrationTest extends AbstractBookingApiIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-10T06:30:00Z");

    @Test
    void adminBookingQueryCountDoesNotGrowWithReturnedBookings() throws Exception {
        User admin = createUser("Admin Query Count", Role.ADMIN);
        User customer = createUser("Admin Query Customer", Role.USER);
        Resource resource = createResource("Admin Query Count Room");
        createBookings(customer, resource, 6);
        Cookie accessToken = login(admin);
        Statistics statistics = hibernateStatistics();

        statistics.clear();
        MvcResult fiveBookingResponse = getAdminBookings(accessToken, Map.of(), 0, 5);
        long fiveBookingQueryCount = statistics.getPrepareStatementCount();

        createBookings(customer, resource, 44);
        statistics.clear();
        MvcResult fiftyBookingResponse = getAdminBookings(accessToken, Map.of(), 0, 50);
        long fiftyBookingQueryCount = statistics.getPrepareStatementCount();

        assertThat(responseBookingIds(fiveBookingResponse)).hasSize(5);
        assertThat(responseBookingIds(fiftyBookingResponse)).hasSize(50);
        assertThat(fiveBookingQueryCount).isPositive().isEqualTo(fiftyBookingQueryCount);
    }

    @Test
    void adminBookingFiltersReturnOnlyMatchingBookings() throws Exception {
        User admin = createUser("Admin Filter", Role.ADMIN);
        User alice = createUser("Alice Filter", Role.USER);
        User bob = createUser("Bob Filter", Role.USER);
        Resource meetingRoom = createResource("Meeting Room");
        Resource interviewRoom = createResource("Interview Room");
        Booking aliceMeeting = createBookings(alice, meetingRoom, 1).getFirst();
        Booking aliceInterview = createBookings(alice, interviewRoom, 1).getFirst();
        Booking bobMeeting = createBookings(bob, meetingRoom, 1).getFirst();
        Cookie accessToken = login(admin);

        assertThat(responseBookingIds(getAdminBookings(
                accessToken, Map.of("userEmail", alice.getEmail()), 0, 20
        ))).containsExactlyInAnyOrder(aliceMeeting.getId(), aliceInterview.getId());

        assertThat(responseBookingIds(getAdminBookings(
                accessToken, Map.of("resourceName", "Meeting"), 0, 20
        ))).containsExactlyInAnyOrder(aliceMeeting.getId(), bobMeeting.getId());

        assertThat(responseBookingIds(getAdminBookings(
                accessToken,
                Map.of("userEmail", alice.getEmail(), "resourceName", "Meeting"),
                0,
                20
        ))).containsExactly(aliceMeeting.getId());
    }

    @Test
    void adminBookingPageSizeIsLimitedToOneHundred() throws Exception {
        User admin = createUser("Admin Page Size", Role.ADMIN);
        User customer = createUser("Admin Page Customer", Role.USER);
        Resource resource = createResource("Admin Page Size Room");
        createBookings(customer, resource, 101);
        Cookie accessToken = login(admin);

        MvcResult maximumPage = getAdminBookings(accessToken, Map.of(), 0, 100);
        assertThat(responseBookingIds(maximumPage)).hasSize(100);
        assertThat(responseBody(maximumPage).path("pageSize").asInt()).isEqualTo(100);
        assertThat(responseBody(maximumPage).path("totalElements").asLong()).isEqualTo(101);

        mockMvc.perform(post("/api/admin/bookings")
                        .cookie(accessToken)
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void regularUserCannotReadAdminBookings() throws Exception {
        User user = createUser("Blocked Admin Reader", Role.USER);

        mockMvc.perform(post("/api/admin/bookings")
                        .cookie(login(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void adminStatsMatchKnownSeededData() throws Exception {
        User admin = createUser("Admin Stats", Role.ADMIN);
        User customer = createUser("Admin Stats Customer", Role.USER);
        Resource firstResource = createResource("Stats Room One");
        Resource secondResource = createResource("Stats Room Two");
        createResource("Stats Room Three");

        List<Booking> todaysBookings = createBookings(customer, firstResource, 4);
        List<Booking> olderBooking = createBookings(customer, firstResource, 1);
        setBookingTimestamp(todaysBookings, NOW.minus(1, ChronoUnit.HOURS));
        setBookingTimestamp(olderBooking, Instant.parse("2026-11-09T18:29:59Z"));

        createSlot(secondResource, NOW.plus(1, ChronoUnit.HOURS), false);
        createSlot(secondResource, NOW.plus(2, ChronoUnit.HOURS), false);
        createSlot(secondResource, NOW.plus(3, ChronoUnit.HOURS), true);
        createSlot(secondResource, NOW.plus(25, ChronoUnit.HOURS), false);

        mockMvc.perform(get("/api/admin/stats").cookie(login(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResources").value(3))
                .andExpect(jsonPath("$.bookingsToday").value(4))
                .andExpect(jsonPath("$.slotsExpiringUnbooked").value(2));
    }

    private MvcResult getAdminBookings(
            Cookie accessToken,
            Map<String, String> filters,
            int page,
            int size
    ) throws Exception {
        return mockMvc.perform(post("/api/admin/bookings")
                        .cookie(accessToken)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
