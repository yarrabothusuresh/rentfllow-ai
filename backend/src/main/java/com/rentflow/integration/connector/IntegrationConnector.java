package com.rentflow.integration.connector;

import com.rentflow.integration.model.ConnectorCapability;
import com.rentflow.integration.model.IntegrationEvent;
import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.model.IntegrationSyncJob;

import java.util.List;

public interface IntegrationConnector {
    IntegrationProvider provider();
    boolean testConnection(String tenantId, String configuration, String credentialReference);
    boolean handleEvent(String tenantId, IntegrationEvent event);
    IntegrationSyncJob sync(String tenantId, IntegrationSyncJob syncJob);
    List<ConnectorCapability> getCapabilities();
}
