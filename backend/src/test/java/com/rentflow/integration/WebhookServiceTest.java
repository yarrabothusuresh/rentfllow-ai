package com.rentflow.integration;

import com.rentflow.integration.dto.CreateWebhookDTO;
import com.rentflow.integration.dto.WebhookDeliveryDTO;
import com.rentflow.integration.dto.WebhookEndpointDTO;
import com.rentflow.integration.model.*;
import com.rentflow.integration.repository.IntegrationOutboxRepository;
import com.rentflow.integration.repository.WebhookDeliveryRepository;
import com.rentflow.integration.service.IntegrationEventService;
import com.rentflow.integration.service.IntegrationOutboxProcessor;
import com.rentflow.integration.service.WebhookDeliveryService;
import com.rentflow.integration.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WebhookServiceTest {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private WebhookDeliveryService deliveryService;

    @Autowired
    private IntegrationEventService eventService;

    @Autowired
    private IntegrationOutboxProcessor outboxProcessor;

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private WebhookDeliveryRepository deliveryRepository;

    private final String tenantId = "test-tenant-webhooks";

    @Test
    public void testCreateWebhookAndSecretMasking() {
        CreateWebhookDTO dto = new CreateWebhookDTO();
        dto.setName("Order Notification Webhook");
        dto.setEndpointUrl("https://example.com/webhook");
        dto.setSubscribedEvents(List.of("booking.created", "invoice.created"));
        dto.setDescription("Test webhook");

        Map<String, Object> result = webhookService.createEndpoint(tenantId, dto, "Admin");

        assertNotNull(result.get("signingSecret"));
        assertTrue(result.get("signingSecret").toString().startsWith("whsec_"));

        WebhookEndpointDTO endpoint = (WebhookEndpointDTO) result.get("endpoint");
        assertEquals("Order Notification Webhook", endpoint.getName());
        assertEquals("https://example.com/webhook", endpoint.getEndpointUrl());
        assertTrue(endpoint.getMaskedSecret().startsWith("••••••••"));
    }

    @Test
    public void testHmacSha256Signature() {
        String secret = "test_signing_secret_1234";
        String payload = "{\"eventId\":\"evt_101\",\"eventType\":\"booking.created\"}";
        String sig1 = deliveryService.generateHmacSha256(secret, payload);
        String sig2 = deliveryService.generateHmacSha256(secret, payload);

        assertNotNull(sig1);
        assertEquals(sig1, sig2);
        assertNotEquals(sig1, deliveryService.generateHmacSha256("different_secret", payload));
    }

    @Test
    public void testOutboxPublishingAndProcessing() {
        // 1. Create Webhook endpoint
        CreateWebhookDTO dto = new CreateWebhookDTO();
        dto.setName("Outbox Test Webhook");
        dto.setEndpointUrl("http://localhost:8080/dev/webhook-receiver");
        dto.setSubscribedEvents(List.of("booking.created"));
        webhookService.createEndpoint(tenantId, dto, "Admin");

        // 2. Publish domain event to outbox
        IntegrationEvent event = eventService.publishEvent(
            tenantId,
            "booking.created",
            "BOOKING",
            "bkg-test-123",
            Map.of("bookingNumber", "BKG-9999", "totalAmount", 1500.0)
        );

        assertNotNull(event.getId());
        assertEquals(IntegrationEventStatus.PENDING, event.getStatus());

        // 3. Verify Outbox record created
        List<IntegrationOutbox> outboxes = outboxRepository.findByTenantId(tenantId);
        assertFalse(outboxes.isEmpty());
        IntegrationOutbox outbox = outboxes.get(0);
        assertEquals("booking.created", outbox.getEventType());

        // 4. Process outbox event
        outboxProcessor.processSingleOutboxEvent(outbox);
        assertEquals(IntegrationEventStatus.COMPLETED, outbox.getStatus());
    }

    @Test
    public void testExponentialBackoffAndRetryLogic() {
        assertEquals(60, deliveryService.calculateBackoffSeconds(1));
        assertEquals(300, deliveryService.calculateBackoffSeconds(2));
        assertEquals(1800, deliveryService.calculateBackoffSeconds(3));
        assertEquals(7200, deliveryService.calculateBackoffSeconds(4));

        assertTrue(deliveryService.isRetryable(500));
        assertTrue(deliveryService.isRetryable(503));
        assertTrue(deliveryService.isRetryable(429));
        assertFalse(deliveryService.isRetryable(400));
        assertFalse(deliveryService.isRetryable(401));
    }

    @Test
    public void testRotateSecret() {
        CreateWebhookDTO dto = new CreateWebhookDTO();
        dto.setName("Rotatable Webhook");
        dto.setEndpointUrl("https://example.com/endpoint");
        Map<String, Object> created = webhookService.createEndpoint(tenantId, dto, "Admin");
        WebhookEndpointDTO ep = (WebhookEndpointDTO) created.get("endpoint");

        var rotated = webhookService.rotateSecret(tenantId, ep.getId());
        assertTrue(rotated.isPresent());
        assertNotNull(rotated.get().get("newSigningSecret"));
        assertNotEquals(created.get("signingSecret"), rotated.get().get("newSigningSecret"));
    }
}
