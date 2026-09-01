package com.rentflow.integration.connector;

import java.util.UUID;

public interface CrmConnector extends IntegrationConnector {
    String syncCustomer(String tenantId, UUID customerId);
    String syncLead(String tenantId, UUID leadId);
    String syncQuote(String tenantId, UUID quoteId);
    String syncBooking(String tenantId, UUID bookingId);
}
