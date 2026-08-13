package com.rentflow.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByTenantId(UUID tenantId);
    Optional<Event> findByIdAndTenantId(UUID id, UUID tenantId);
}
