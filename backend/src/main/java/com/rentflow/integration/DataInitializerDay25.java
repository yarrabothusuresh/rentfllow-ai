package com.rentflow.integration;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.integration.model.*;
import com.rentflow.integration.repository.*;
import com.rentflow.integration.service.IntegrationCredentialService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Order(25)
public class DataInitializerDay25 implements CommandLineRunner {

    private final IntegrationConnectionRepository connectionRepository;
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final ExternalApiKeyRepository apiKeyRepository;
    private final ExternalEntityMappingRepository mappingRepository;
    private final IntegrationEventRepository eventRepository;
    private final IntegrationCredentialService credentialService;

    public DataInitializerDay25(IntegrationConnectionRepository connectionRepository,
                                WebhookEndpointRepository webhookEndpointRepository,
                                WebhookDeliveryRepository webhookDeliveryRepository,
                                ExternalApiKeyRepository apiKeyRepository,
                                ExternalEntityMappingRepository mappingRepository,
                                IntegrationEventRepository eventRepository,
                                IntegrationCredentialService credentialService) {
        this.connectionRepository = connectionRepository;
        this.webhookEndpointRepository = webhookEndpointRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.mappingRepository = mappingRepository;
        this.eventRepository = eventRepository;
        this.credentialService = credentialService;
    }

    @Override
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (webhookEndpointRepository.countByTenantIdAndStatus(tenantId, WebhookStatus.ACTIVE) > 0) {
            return;
        }

        // 1. Seed Active Demo Webhook
        WebhookEndpoint demoWebhook = new WebhookEndpoint();
        demoWebhook.setTenantId(tenantId);
        demoWebhook.setName("Zapier Booking & Invoice Automation");
        demoWebhook.setEndpointUrl("http://localhost:8080/dev/webhook-receiver");
        demoWebhook.setSubscribedEvents("booking.created,invoice.created,payment.received,damage_claim.created");
        demoWebhook.setDescription("Dispatches events to Zapier for Slack notifications and external spreadsheets");
        demoWebhook.setStatus(WebhookStatus.ACTIVE);
        demoWebhook.setCreatedBy("Operations Manager");
        demoWebhook.setSecretReference(credentialService.encryptCredential("whsec_demo_evergreen_8899aabbcc"));
        demoWebhook = webhookEndpointRepository.save(demoWebhook);

        // 2. Seed Mock QuickBooks Connection
        IntegrationConnection qbConn = new IntegrationConnection();
        qbConn.setTenantId(tenantId);
        qbConn.setProvider(IntegrationProvider.MOCK_ACCOUNTING);
        qbConn.setName("QuickBooks Online (Sandbox)");
        qbConn.setStatus(ConnectionStatus.CONNECTED);
        qbConn.setConfiguration("{\"realmId\":\"913034789012345\",\"environment\":\"sandbox\",\"syncInvoices\":true,\"syncPayments\":true}");
        qbConn.setCredentialReference(credentialService.encryptCredential("oauth_qb_refresh_demo_token_1234"));
        qbConn.setLastConnectedAt(LocalDateTime.now().minusDays(3));
        qbConn.setLastSyncAt(LocalDateTime.now().minusHours(2));
        qbConn.setCreatedBy("Operations Manager");
        connectionRepository.save(qbConn);

        // 3. Seed Mock HubSpot CRM Connection
        IntegrationConnection hsConn = new IntegrationConnection();
        hsConn.setTenantId(tenantId);
        hsConn.setProvider(IntegrationProvider.MOCK_CRM);
        hsConn.setName("HubSpot CRM (Sales Pipeline)");
        hsConn.setStatus(ConnectionStatus.CONNECTED);
        hsConn.setConfiguration("{\"portalId\":\"24890123\",\"pipeline\":\"Rental Sales Pipeline\",\"syncCustomers\":true}");
        hsConn.setCredentialReference(credentialService.encryptCredential("pat_na1_demo_hubspot_token_9876"));
        hsConn.setLastConnectedAt(LocalDateTime.now().minusDays(5));
        hsConn.setLastSyncAt(LocalDateTime.now().minusHours(4));
        hsConn.setCreatedBy("Operations Manager");
        connectionRepository.save(hsConn);

        // 4. Seed External API Keys
        ExternalApiKey liveKey = new ExternalApiKey();
        liveKey.setTenantId(tenantId);
        liveKey.setName("Mobile Catalog & Storefront App");
        liveKey.setKeyPrefix("rf_live_app1...");
        liveKey.setKeyHash(credentialService.hashSecret("rf_live_demo_catalog_app_secret_key_12345"));
        liveKey.setStatus(ApiKeyStatus.ACTIVE);
        liveKey.setScopes("products:read,inventory:read,quotes:write");
        liveKey.setRateLimitPerMinute(120);
        liveKey.setLastUsedAt(LocalDateTime.now().minusMinutes(15));
        liveKey.setCreatedBy("Operations Manager");
        apiKeyRepository.save(liveKey);

        ExternalApiKey bkgKey = new ExternalApiKey();
        bkgKey.setTenantId(tenantId);
        bkgKey.setName("Partner Booking Portal Integration");
        bkgKey.setKeyPrefix("rf_live_prtn...");
        bkgKey.setKeyHash(credentialService.hashSecret("rf_live_demo_partner_portal_key_67890"));
        bkgKey.setStatus(ApiKeyStatus.ACTIVE);
        bkgKey.setScopes("customers:read,customers:write,bookings:read,invoices:read");
        bkgKey.setRateLimitPerMinute(200);
        bkgKey.setLastUsedAt(LocalDateTime.now().minusHours(1));
        bkgKey.setCreatedBy("Operations Manager");
        apiKeyRepository.save(bkgKey);

        // 5. Seed Historical Integration Events & Deliveries
        String evtId1 = "evt_" + UUID.randomUUID().toString().replace("-", "");
        IntegrationEvent evt1 = new IntegrationEvent();
        evt1.setTenantId(tenantId);
        evt1.setEventId(evtId1);
        evt1.setEventType("booking.created");
        evt1.setAggregateType("BOOKING");
        evt1.setAggregateId("bkg-001");
        evt1.setPayload("{\"eventId\":\"" + evtId1 + "\",\"eventType\":\"booking.created\",\"eventVersion\":\"1.0\",\"tenantId\":\"" +
            tenantId + "\",\"occurredAt\":\"" + LocalDateTime.now().minusHours(3) + "\",\"data\":{\"bookingNumber\":\"BKG-000101\",\"customerName\":\"Apex Luxury Events\",\"totalAmount\":2840.00}}");
        evt1.setStatus(IntegrationEventStatus.COMPLETED);
        evt1.setOccurredAt(LocalDateTime.now().minusHours(3));
        eventRepository.save(evt1);

        WebhookDelivery del1 = new WebhookDelivery();
        del1.setTenantId(tenantId);
        del1.setWebhookEndpointId(demoWebhook.getId());
        del1.setIntegrationEventId(evtId1);
        del1.setEventType("booking.created");
        del1.setAttemptNumber(1);
        del1.setStatus(DeliveryStatus.SUCCESS);
        del1.setHttpStatus(200);
        del1.setResponseSummary("{\"status\":\"SUCCESS\",\"receivedEventId\":\"" + evtId1 + "\"}");
        del1.setRequestHeaders("X-RentFlow-Event-Id: " + evtId1 + "\nX-RentFlow-Event-Type: booking.created");
        del1.setRequestPayload(evt1.getPayload());
        del1.setDurationMs(84L);
        del1.setStartedAt(LocalDateTime.now().minusHours(3));
        del1.setCompletedAt(LocalDateTime.now().minusHours(3));
        webhookDeliveryRepository.save(del1);

        // Seed Sample Dead-Letter Delivery
        String evtId2 = "evt_" + UUID.randomUUID().toString().replace("-", "");
        IntegrationEvent evt2 = new IntegrationEvent();
        evt2.setTenantId(tenantId);
        evt2.setEventId(evtId2);
        evt2.setEventType("payment.received");
        evt2.setAggregateType("PAYMENT");
        evt2.setAggregateId("pmt-002");
        evt2.setPayload("{\"eventId\":\"" + evtId2 + "\",\"eventType\":\"payment.received\",\"eventVersion\":\"1.0\",\"tenantId\":\"" +
            tenantId + "\",\"occurredAt\":\"" + LocalDateTime.now().minusHours(5) + "\",\"data\":{\"amount\":1200.00,\"paymentId\":\"pmt-002\"}}");
        evt2.setStatus(IntegrationEventStatus.DEAD_LETTER);
        evt2.setOccurredAt(LocalDateTime.now().minusHours(5));
        eventRepository.save(evt2);

        WebhookDelivery deadDelivery = new WebhookDelivery();
        deadDelivery.setTenantId(tenantId);
        deadDelivery.setWebhookEndpointId(demoWebhook.getId());
        deadDelivery.setIntegrationEventId(evtId2);
        deadDelivery.setEventType("payment.received");
        deadDelivery.setAttemptNumber(5);
        deadDelivery.setStatus(DeliveryStatus.DEAD_LETTER);
        deadDelivery.setHttpStatus(503);
        deadDelivery.setResponseSummary("HTTP 503: Service Unavailable (Remote Zapier webhook endpoint timed out)");
        deadDelivery.setRequestHeaders("X-RentFlow-Event-Id: " + evtId2 + "\nX-RentFlow-Event-Type: payment.received");
        deadDelivery.setRequestPayload(evt2.getPayload());
        deadDelivery.setDurationMs(5002L);
        deadDelivery.setStartedAt(LocalDateTime.now().minusHours(5));
        deadDelivery.setCompletedAt(LocalDateTime.now().minusHours(5));
        webhookDeliveryRepository.save(deadDelivery);

        // Seed External Entity Mappings
        ExternalEntityMapping map1 = new ExternalEntityMapping(
            tenantId,
            IntegrationProvider.MOCK_ACCOUNTING,
            EntityType.INVOICE,
            "inv-000101",
            "MOCK-QB-INV-88390",
            "SYNCED"
        );
        mappingRepository.save(map1);

        ExternalEntityMapping map2 = new ExternalEntityMapping(
            tenantId,
            IntegrationProvider.MOCK_CRM,
            EntityType.CUSTOMER,
            "cust-000101",
            "MOCK-HS-CUST-77210",
            "SYNCED"
        );
        mappingRepository.save(map2);
    }
}
