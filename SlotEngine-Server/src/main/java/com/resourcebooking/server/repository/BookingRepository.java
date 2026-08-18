package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsBySlotIdAndStatus(Long slotId, BookingStatus status);

    long countBySlotIdAndStatus(Long slotId, BookingStatus status);

    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);
}