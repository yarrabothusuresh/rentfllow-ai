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
public class MockCommerceConnector implements CommerceConnector {

    private static final Logger log = LoggerFactory.getLogger(MockCommerceConnector.class);
    private final ExternalEntityMappingRepository mappingRepository;

    public MockCommerceConnector(ExternalEntityMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    @Override
    public IntegrationProvider provider() {
        return IntegrationProvider.MOCK_COMMERCE;
    }

    @Override
    public boolean testConnection(String tenantId, String configuration, String credentialReference) {
        log.info("[MockCommerce] Connection test successful for tenant {}", tenantId);
        return true;
    }

    @Override
    public boolean handleEvent(String tenantId, IntegrationEvent event) {
        log.info("[MockCommerce] Processing event {} for aggregate {}", event.getEventType(), event.getAggregateId());
        if ("product.created".equals(event.getEventType()) || "product.updated".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncProduct(tenantId, UUID.fromString(event.getAggregateId()));
            }
        }
        return true;
    }

    @Override
    public IntegrationSyncJob sync(String tenantId, IntegrationSyncJob syncJob) {
        log.info("[MockCommerce] Starting manual catalog sync for tenant {}", tenantId);
        syncJob.setStatus(SyncJobStatus.RUNNING);
        syncJob.setStartedAt(LocalDateTime.now());
        syncJob.setRecordsProcessed(40);
        syncJob.setRecordsFailed(0);
        syncJob.setStatus(SyncJobStatus.COMPLETED);
        syncJob.setCompletedAt(LocalDateTime.now());
        return syncJob;
    }

    @Override
    public List<ConnectorCapability> getCapabilities() {
        return Arrays.asList(
            ConnectorCapability.PRODUCTS,
            ConnectorCapability.INVENTORY,
            ConnectorCapability.CUSTOMERS
        );
    }

    @Override
    public String syncProduct(String tenantId, UUID productId) {
        String extId = "MOCK-SHPFY-PROD-" + productId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.PRODUCT, productId.toString(), extId);
        log.info("[MockCommerce] Synced product {} -> {}", productId, extId);
        return extId;
    }

    @Override
    public boolean syncAvailability(String tenantId, UUID productId) {
        log.info("[MockCommerce] Date-aware availability synced for product {}", productId);
        return true;
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
