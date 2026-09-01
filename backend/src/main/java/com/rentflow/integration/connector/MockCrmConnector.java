package com.rentflow.integration.connector;

import com.rentflow.integration.model.*;
import com.rentflow.integration.repository.ExternalEntityMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MockCrmConnector implements CrmConnector {

    private static final Logger log = LoggerFactory.getLogger(MockCrmConnector.class);
    private final ExternalEntityMappingRepository mappingRepository;

    public MockCrmConnector(ExternalEntityMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    @Override
    public IntegrationProvider provider() {
        return IntegrationProvider.MOCK_CRM;
    }

    @Override
    public boolean testConnection(String tenantId, String configuration, String credentialReference) {
        log.info("[MockCRM] Connection test successful for tenant {}", tenantId);
        return true;
    }

    @Override
    public boolean handleEvent(String tenantId, IntegrationEvent event) {
        log.info("[MockCRM] Processing event {} for aggregate {}", event.getEventType(), event.getAggregateId());
        if ("customer.created".equals(event.getEventType()) || "CUSTOMER_CREATED".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncCustomer(tenantId, UUID.fromString(event.getAggregateId()));
            }
            return true;
        } else if ("quote.approved".equals(event.getEventType()) || "QUOTE_APPROVED".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncQuote(tenantId, UUID.fromString(event.getAggregateId()));
            }
            return true;
        } else if ("booking.created".equals(event.getEventType()) || "BOOKING_CREATED".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncBooking(tenantId, UUID.fromString(event.getAggregateId()));
            }
            return true;
        }
        return true;
    }

    @Override
    public IntegrationSyncJob sync(String tenantId, IntegrationSyncJob syncJob) {
        log.info("[MockCRM] Starting manual CRM sync for tenant {}", tenantId);
        syncJob.setStatus(SyncJobStatus.RUNNING);
        syncJob.setStartedAt(LocalDateTime.now());
        
        syncJob.setRecordsProcessed(22);
        syncJob.setRecordsFailed(0);
        syncJob.setStatus(SyncJobStatus.COMPLETED);
        syncJob.setCompletedAt(LocalDateTime.now());
        syncJob.setErrorSummary(null);
        return syncJob;
    }

    @Override
    public List<ConnectorCapability> getCapabilities() {
        return Arrays.asList(
            ConnectorCapability.CUSTOMERS,
            ConnectorCapability.QUOTES,
            ConnectorCapability.BOOKINGS
        );
    }

    @Override
    public String syncCustomer(String tenantId, UUID customerId) {
        String extId = "MOCK-HS-CUST-" + customerId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.CUSTOMER, customerId.toString(), extId);
        log.info("[MockCRM] Synced customer {} -> {}", customerId, extId);
        return extId;
    }

    @Override
    public String syncLead(String tenantId, UUID leadId) {
        String extId = "MOCK-HS-LEAD-" + leadId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.CUSTOMER, leadId.toString(), extId);
        log.info("[MockCRM] Synced lead {} -> {}", leadId, extId);
        return extId;
    }

    @Override
    public String syncQuote(String tenantId, UUID quoteId) {
        String extId = "MOCK-HS-DEAL-" + quoteId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.QUOTE, quoteId.toString(), extId);
        log.info("[MockCRM] Synced quote {} -> {}", quoteId, extId);
        return extId;
    }

    @Override
    public String syncBooking(String tenantId, UUID bookingId) {
        String extId = "MOCK-HS-DEAL-" + bookingId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.BOOKING, bookingId.toString(), extId);
        log.info("[MockCRM] Synced booking {} -> {}", bookingId, extId);
        return extId;
    }

    private void saveMapping(String tenantId, EntityType entityType, String internalId, String externalId) {
        Optional<ExternalEntityMapping> existing = mappingRepository.findByTenantIdAndProviderAndEntityTypeAndInternalId(
            tenantId, provider(), entityType, internalId);
        if (existing.isPresent()) {
            ExternalEntityMapping mapping = existing.get();
            mapping.setExternalId(externalId);
            mapping.setSyncStatus("SYNCED");
            mapping.setLastSyncedAt(LocalDateTime.now());
            mappingRepository.save(mapping);
        } else {
            ExternalEntityMapping mapping = new ExternalEntityMapping(
                tenantId, provider(), entityType, internalId, externalId, "SYNCED");
            mappingRepository.save(mapping);
        }
    }
}
