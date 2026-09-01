package com.rentflow.integration.repository;

import com.rentflow.integration.model.IntegrationEventStatus;
import com.rentflow.integration.model.IntegrationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationOutboxRepository extends JpaRepository<IntegrationOutbox, UUID> {
    List<IntegrationOutbox> findByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(IntegrationEventStatus status, LocalDateTime before);
    List<IntegrationOutbox> findByTenantId(String tenantId);
    Optional<IntegrationOutbox> findByTenantIdAndEventId(String tenantId, String eventId);
    List<IntegrationOutbox> findByTenantIdAndStatus(String tenantId, IntegrationEventStatus status);
}
