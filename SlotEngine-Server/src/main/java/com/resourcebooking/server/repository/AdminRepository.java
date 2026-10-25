package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface AdminRepository extends Repository<Booking, Long> {

    @EntityGraph(attributePaths = {"user", "slot", "slot.resource"})
    @Query(value = "select b from Booking b", countQuery = "select count(b) from Booking b")
    Page<Booking> findAllForAdmin(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "slot", "slot.resource"})
    Page<Booking> findByUserEmailContainingIgnoreCase(String userEmail, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "slot", "slot.resource"})
    Page<Booking> findBySlotResourceNameContainingIgnoreCase(String resourceName, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "slot", "slot.resource"})
    Page<Booking> findByUserEmailContainingIgnoreCaseAndSlotResourceNameContainingIgnoreCase(
            String userEmail, String resourceName, Pageable pageable);

    @Query(value = """
            SELECT
                (SELECT count(*) FROM resources) AS "totalResources",
                (SELECT count(*) FROM bookings
                 WHERE booked_at >= :todayStart AND booked_at < :now) AS "bookingsToday",
                (SELECT count(*) FROM slots
                 WHERE is_booked = false
                   AND slot_start >= :now AND slot_start < :expiringBefore) AS "slotsExpiringUnbooked"
            """, nativeQuery = true)
    AdminStatsCounts getStats(
            @Param("todayStart") OffsetDateTime todayStart,
            @Param("now") OffsetDateTime now,
            @Param("expiringBefore") OffsetDateTime expiringBefore
    );

    interface AdminStatsCounts {
        long getTotalResources();
        long getBookingsToday();
        long getSlotsExpiringUnbooked();
    }
}
