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
public class MockAccountingConnector implements AccountingConnector {

    private static final Logger log = LoggerFactory.getLogger(MockAccountingConnector.class);
    private final ExternalEntityMappingRepository mappingRepository;

    public MockAccountingConnector(ExternalEntityMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    @Override
    public IntegrationProvider provider() {
        return IntegrationProvider.MOCK_ACCOUNTING;
    }

    @Override
    public boolean testConnection(String tenantId, String configuration, String credentialReference) {
        log.info("[MockAccounting] Connection test successful for tenant {}", tenantId);
        return true;
    }

    @Override
    public boolean handleEvent(String tenantId, IntegrationEvent event) {
        log.info("[MockAccounting] Processing event {} for aggregate {}", event.getEventType(), event.getAggregateId());
        if ("invoice.created".equals(event.getEventType()) || "INVOICE_CREATED".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncInvoice(tenantId, UUID.fromString(event.getAggregateId()));
            }
            return true;
        } else if ("customer.created".equals(event.getEventType()) || "CUSTOMER_CREATED".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncCustomer(tenantId, UUID.fromString(event.getAggregateId()));
            }
            return true;
        } else if ("payment.received".equals(event.getEventType()) || "PAYMENT_RECEIVED".equals(event.getEventType())) {
            if (event.getAggregateId() != null) {
                syncPayment(tenantId, UUID.fromString(event.getAggregateId()));
            }
            return true;
        }
        return true;
    }

    @Override
    public IntegrationSyncJob sync(String tenantId, IntegrationSyncJob syncJob) {
        log.info("[MockAccounting] Starting manual sync for tenant {}", tenantId);
        syncJob.setStatus(SyncJobStatus.RUNNING);
        syncJob.setStartedAt(LocalDateTime.now());
        
        // Mock sync logic
        syncJob.setRecordsProcessed(15);
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
            ConnectorCapability.INVOICES,
            ConnectorCapability.PAYMENTS,
            ConnectorCapability.REFUNDS
        );
    }

    @Override
    public String syncCustomer(String tenantId, UUID customerId) {
        String extId = "MOCK-QB-CUST-" + customerId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.CUSTOMER, customerId.toString(), extId);
        log.info("[MockAccounting] Synced customer {} -> {}", customerId, extId);
        return extId;
    }

    @Override
    public String syncInvoice(String tenantId, UUID invoiceId) {
        String extId = "MOCK-QB-INV-" + invoiceId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.INVOICE, invoiceId.toString(), extId);
        log.info("[MockAccounting] Synced invoice {} -> {}", invoiceId, extId);
        return extId;
    }

    @Override
    public String syncPayment(String tenantId, UUID paymentId) {
        String extId = "MOCK-QB-PMT-" + paymentId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.PAYMENT, paymentId.toString(), extId);
        log.info("[MockAccounting] Synced payment {} -> {}", paymentId, extId);
        return extId;
    }

    @Override
    public String syncRefund(String tenantId, UUID refundId) {
        String extId = "MOCK-QB-REF-" + refundId.toString().substring(0, 8).toUpperCase();
        saveMapping(tenantId, EntityType.PAYMENT, refundId.toString(), extId);
        log.info("[MockAccounting] Synced refund {} -> {}", refundId, extId);
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
