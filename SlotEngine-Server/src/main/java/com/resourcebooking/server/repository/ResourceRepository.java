package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
