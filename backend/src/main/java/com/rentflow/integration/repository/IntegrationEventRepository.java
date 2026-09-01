package com.rentflow.integration.repository;

import com.rentflow.integration.model.IntegrationEvent;
import com.rentflow.integration.model.IntegrationEventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, UUID> {
    List<IntegrationEvent> findByTenantId(String tenantId);
    Page<IntegrationEvent> findByTenantIdOrderByOccurredAtDesc(String tenantId, Pageable pageable);
    Optional<IntegrationEvent> findByTenantIdAndEventId(String tenantId, String eventId);
    List<IntegrationEvent> findByTenantIdAndStatus(String tenantId, IntegrationEventStatus status);
    long countByTenantIdAndOccurredAtAfter(String tenantId, LocalDateTime after);
}
