package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.Resource;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("select resource from Resource resource where resource.id = :id")
    Optional<Resource> findByIdForUpdate(Long id);
}
