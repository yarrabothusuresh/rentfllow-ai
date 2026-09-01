package com.rentflow.integration;

import com.rentflow.integration.connector.MockAccountingConnector;
import com.rentflow.integration.connector.MockCrmConnector;
import com.rentflow.integration.dto.*;
import com.rentflow.integration.model.*;
import com.rentflow.integration.service.IntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class IntegrationServiceTest {

    @Autowired
    private IntegrationService integrationService;

    @Autowired
    private MockAccountingConnector accountingConnector;

    @Autowired
    private MockCrmConnector crmConnector;

    private final String tenantId = "test-tenant-day25";

    @Test
    public void testConnectAndListProviders() {
        ConnectIntegrationDTO dto = new ConnectIntegrationDTO();
        dto.setProvider(IntegrationProvider.MOCK_ACCOUNTING);
        dto.setName("QuickBooks Production");
        dto.setConfiguration("{\"sandbox\":false}");
        dto.setApiKeyOrSecret("super_secret_qb_oauth_token");

        IntegrationConnectionDTO conn = integrationService.connect(tenantId, dto, "Admin User");

        assertNotNull(conn.getId());
        assertEquals(IntegrationProvider.MOCK_ACCOUNTING, conn.getProvider());
        assertEquals(ConnectionStatus.CONNECTED, conn.getStatus());
        assertTrue(conn.getMaskedCredential().startsWith("••••••••"));
        assertFalse(conn.getCapabilities().isEmpty());

        List<IntegrationConnectionDTO> list = integrationService.getConnections(tenantId);
        assertEquals(1, list.size());
    }

    @Test
    public void testConnectionHealthAndSync() {
        ConnectIntegrationDTO dto = new ConnectIntegrationDTO();
        dto.setProvider(IntegrationProvider.MOCK_CRM);
        dto.setName("HubSpot Integration");
        IntegrationConnectionDTO conn = integrationService.connect(tenantId, dto, "Admin");

        boolean health = integrationService.testConnection(tenantId, conn.getId());
        assertTrue(health);

        IntegrationSyncJobDTO syncJob = integrationService.triggerSync(tenantId, conn.getId());
        assertNotNull(syncJob.getId());
        assertEquals(SyncJobStatus.COMPLETED, syncJob.getStatus());
        assertTrue(syncJob.getRecordsProcessed() > 0);
    }

    @Test
    public void testDisconnectProvider() {
        ConnectIntegrationDTO dto = new ConnectIntegrationDTO();
        dto.setProvider(IntegrationProvider.ZAPIER);
        dto.setName("Zapier Automation");
        IntegrationConnectionDTO conn = integrationService.connect(tenantId, dto, "Admin");

        var disconnected = integrationService.disconnect(tenantId, conn.getId());
        assertTrue(disconnected.isPresent());
        assertEquals(ConnectionStatus.DISCONNECTED, disconnected.get().getStatus());
    }

    @Test
    public void testMockAccountingEntityMapping() {
        UUID invoiceId = UUID.randomUUID();
        String externalId = accountingConnector.syncInvoice(tenantId, invoiceId);

        assertNotNull(externalId);
        assertTrue(externalId.startsWith("MOCK-QB-INV-"));

        List<ExternalEntityMappingDTO> mappings = integrationService.getEntityMappings(tenantId, IntegrationProvider.MOCK_ACCOUNTING);
        assertFalse(mappings.isEmpty());
        assertEquals(invoiceId.toString(), mappings.get(0).getInternalId());
        assertEquals(externalId, mappings.get(0).getExternalId());
    }

    @Test
    public void testMockCrmEntityMapping() {
        UUID customerId = UUID.randomUUID();
        String externalId = crmConnector.syncCustomer(tenantId, customerId);

        assertNotNull(externalId);
        assertTrue(externalId.startsWith("MOCK-HS-CUST-"));

        List<ExternalEntityMappingDTO> mappings = integrationService.getEntityMappings(tenantId, IntegrationProvider.MOCK_CRM);
        assertFalse(mappings.isEmpty());
        assertEquals(customerId.toString(), mappings.get(0).getInternalId());
    }

    @Test
    public void testDashboardSummary() {
        IntegrationDashboardSummaryDTO summary = integrationService.getDashboardSummary(tenantId);
        assertNotNull(summary);
        assertTrue(summary.getSuccessRate() >= 0.0);
    }
}
