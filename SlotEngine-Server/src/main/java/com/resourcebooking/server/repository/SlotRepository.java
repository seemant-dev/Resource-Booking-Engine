package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Query("""
            select s
            from Slot s
            where s.resource.id = :resourceId
              and s.slotStart >= :startInclusive
              and s.slotStart < :endExclusive
            order by s.slotStart asc
            """)
    List<Slot> findByResourceIdAndSlotStartBetween(
            Long resourceId,
            Instant startInclusive,
            Instant endExclusive
    );

    boolean existsByResourceIdAndSlotStartBeforeAndSlotEndAfter(
            Long resourceId,
            Instant candidateEnd,
            Instant candidateStart
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from Slot s
            where s.id = :id
            """)
    Optional<Slot> findByIdWithLock(Long id);
}