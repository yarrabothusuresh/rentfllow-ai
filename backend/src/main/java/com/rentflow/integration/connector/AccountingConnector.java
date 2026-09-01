package com.rentflow.integration.connector;

import java.util.UUID;

public interface AccountingConnector extends IntegrationConnector {
    String syncCustomer(String tenantId, UUID customerId);
    String syncInvoice(String tenantId, UUID invoiceId);
    String syncPayment(String tenantId, UUID paymentId);
    String syncRefund(String tenantId, UUID refundId);
}
