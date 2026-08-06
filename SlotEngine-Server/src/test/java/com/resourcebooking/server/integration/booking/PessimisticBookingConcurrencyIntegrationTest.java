package com.resourcebooking.server.integration.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "booking.locking-strategy=pessimistic")
class PessimisticBookingConcurrencyIntegrationTest extends AbstractBookingConcurrencyIntegrationTest {

    @Test
    void pessimisticLockingAllowsOnlyOneSuccessfulBookingForSameSlot() throws Exception {
        verifyOnlyOneBookingSucceedsUnderConcurrentLoad("pessimistic");
    }
}