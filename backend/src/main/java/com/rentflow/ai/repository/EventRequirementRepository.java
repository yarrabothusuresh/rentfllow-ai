package com.rentflow.ai.repository;

import com.rentflow.ai.model.EventRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRequirementRepository extends JpaRepository<EventRequirement, UUID> {
    List<EventRequirement> findByTenantIdAndEventId(String tenantId, UUID eventId);
    Optional<EventRequirement> findByTenantIdAndId(String tenantId, UUID id);
    void deleteByTenantIdAndEventId(String tenantId, UUID eventId);
}
