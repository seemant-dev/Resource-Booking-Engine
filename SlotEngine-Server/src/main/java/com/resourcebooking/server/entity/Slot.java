package com.resourcebooking.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Table(
        name = "slots",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_slots_resource_start",
                    columnNames = {"resource_id", "slot_start"}
            )
        },
        indexes = {
                @Index(
                        name = "idx_slots_resource_start",
                        columnList = "resource_id, slot_start"
                )
        }
)
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Setter
    @Column(name = "slot_start", nullable = false)
    private Instant slotStart;

    @Setter
    @Column(name = "slot_end", nullable = false)
    private Instant slotEnd;

    @Setter
    @Column(name = "is_booked", nullable = false)
    private boolean booked = false;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
