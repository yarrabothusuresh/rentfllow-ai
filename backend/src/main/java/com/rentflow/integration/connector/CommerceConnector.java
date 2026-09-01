package com.rentflow.integration.connector;

import java.util.UUID;

public interface CommerceConnector extends IntegrationConnector {
    String syncProduct(String tenantId, UUID productId);
    boolean syncAvailability(String tenantId, UUID productId);
}
