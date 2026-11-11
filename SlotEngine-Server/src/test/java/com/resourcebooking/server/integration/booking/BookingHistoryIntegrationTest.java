package com.resourcebooking.server.integration.booking;

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
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@AutoConfigureMockMvc
class BookingHistoryIntegrationTest extends AbstractBookingApiIntegrationTest {

    @Test
    void pageZeroAndPageOneReturnFiftyDistinctBookingsEach() throws Exception {
        User user = createUser("History Pagination User", Role.USER);
        Resource resource = createResource("History Pagination Room");
        createBookings(user, resource, 100);
        Cookie accessToken = login(user);

        MvcResult firstPage = getHistory(accessToken, 0);
        MvcResult secondPage = getHistory(accessToken, 1);

        List<Long> firstPageIds = responseBookingIds(firstPage);
        List<Long> secondPageIds = responseBookingIds(secondPage);

        assertThat(firstPageIds).hasSize(50).doesNotHaveDuplicates();
        assertThat(secondPageIds).hasSize(50).doesNotHaveDuplicates();
        assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
        assertThat(new HashSet<>(firstPageIds)).doesNotContainAnyElementsOf(secondPageIds);
    }

    @Test
    void outOfRangePageReturnsEmptyContent() throws Exception {
        User user = createUser("History Out Of Range User", Role.USER);
        Resource resource = createResource("History Out Of Range Room");
        createBookings(user, resource, 1);

        mockMvc.perform(get("/api/bookings/me")
                        .cookie(login(user))
                        .param("page", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void negativePageReturnsBadRequest() throws Exception {
        User user = createUser("History Invalid Page User", Role.USER);

        mockMvc.perform(get("/api/bookings/me")
                        .cookie(login(user))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void historyContainsOnlyAuthenticatedUsersBookings() throws Exception {
        User firstUser = createUser("History First User", Role.USER);
        User secondUser = createUser("History Second User", Role.USER);
        Resource resource = createResource("History Isolation Room");
        List<Booking> firstUsersBookings = createBookings(firstUser, resource, 3);
        List<Booking> secondUsersBookings = createBookings(secondUser, resource, 2);

        List<Long> returnedIds = responseBookingIds(getHistory(login(firstUser), 0));
        List<Long> expectedIds = firstUsersBookings.stream().map(Booking::getId).toList();
        List<Long> otherUsersIds = secondUsersBookings.stream().map(Booking::getId).toList();

        assertThat(returnedIds).containsExactlyInAnyOrderElementsOf(expectedIds);
        assertThat(returnedIds).doesNotContainAnyElementsOf(otherUsersIds);
    }

    @Test
    void bookingsWithSameTimestampAreOrderedByIdDescending() throws Exception {
        User user = createUser("History Stable Order User", Role.USER);
        Resource resource = createResource("History Stable Order Room");
        List<Booking> bookings = createBookings(user, resource, 3);
        setBookingTimestamp(bookings, Instant.parse("2026-11-10T06:30:00Z"));

        List<Long> expectedIds = bookings.stream()
                .map(Booking::getId)
                .sorted(Comparator.reverseOrder())
                .toList();

        assertThat(responseBookingIds(getHistory(login(user), 0)))
                .containsExactlyElementsOf(expectedIds);
    }

    @Test
    void historyQueryCountRemainsBoundedAsReturnedBookingsGrow() throws Exception {
        User user = createUser("History Query Count User", Role.USER);
        Resource resource = createResource("History Query Count Room");
        createBookings(user, resource, 5);
        Cookie accessToken = login(user);
        Statistics statistics = hibernateStatistics();

        statistics.clear();
        MvcResult fiveBookingResponse = getHistory(accessToken, 0);
        long fiveBookingStatementCount = statistics.getPrepareStatementCount();
        long fiveBookingEntityFetchCount = statistics.getEntityFetchCount();

        createBookings(user, resource, 45);
        statistics.clear();
        MvcResult fiftyBookingResponse = getHistory(accessToken, 0);
        long fiftyBookingStatementCount = statistics.getPrepareStatementCount();
        long fiftyBookingEntityFetchCount = statistics.getEntityFetchCount();

        assertThat(responseBookingIds(fiveBookingResponse)).hasSize(5);
        assertThat(responseBookingIds(fiftyBookingResponse)).hasSize(50);
        assertThat(fiveBookingEntityFetchCount).isZero();
        assertThat(fiftyBookingEntityFetchCount).isEqualTo(fiveBookingEntityFetchCount);
        assertThat(fiveBookingStatementCount).isPositive();
        assertThat(fiftyBookingStatementCount).isLessThanOrEqualTo(fiveBookingStatementCount + 1);
    }

    private MvcResult getHistory(Cookie accessToken, int page) throws Exception {
        return mockMvc.perform(get("/api/bookings/me")
                        .cookie(accessToken)
                        .param("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andReturn();
    }
}
