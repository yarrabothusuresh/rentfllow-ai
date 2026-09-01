package com.rentflow.integration.repository;

import com.rentflow.integration.model.InboundEventStatus;
import com.rentflow.integration.model.InboundIntegrationEvent;
import com.rentflow.integration.model.IntegrationProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboundIntegrationEventRepository extends JpaRepository<InboundIntegrationEvent, UUID> {
    List<InboundIntegrationEvent> findByTenantId(String tenantId);
    Optional<InboundIntegrationEvent> findByTenantIdAndProviderAndExternalEventId(
        String tenantId, IntegrationProvider provider, String externalEventId);
    List<InboundIntegrationEvent> findByTenantIdAndStatus(String tenantId, InboundEventStatus status);
}
