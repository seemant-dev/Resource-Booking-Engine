package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.Booking;
import com.resourcebooking.server.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsBySlotIdAndStatus(Long slotId, BookingStatus status);

    long countBySlotIdAndStatus(Long slotId, BookingStatus status);

    Page<Booking> findByUserId(Long userId, Pageable pageable);
}