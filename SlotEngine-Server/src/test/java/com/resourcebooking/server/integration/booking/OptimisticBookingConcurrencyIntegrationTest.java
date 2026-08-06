package com.resourcebooking.server.integration.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "booking.locking-strategy=optimistic")
class OptimisticBookingConcurrencyIntegrationTest extends AbstractBookingConcurrencyIntegrationTest {

    @Test
    void optimisticLockingAllowsOnlyOneSuccessfulBookingForSameSlot() throws Exception {
        verifyOnlyOneBookingSucceedsUnderConcurrentLoad("optimistic");
    }
}