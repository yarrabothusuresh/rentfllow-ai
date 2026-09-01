package com.rentflow.integration;

import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.service.InboundWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class InboundWebhookServiceTest {

    @Autowired
    private InboundWebhookService inboundWebhookService;

    private final String tenantId = "test-tenant-inbound";

    @Test
    public void testInboundEventProcessingAndDeduplication() {
        String extEventId = "stripe_evt_99887766";
        String payload = "{\"id\":\"evt_1\",\"type\":\"payment_intent.succeeded\"}";

        // First delivery: should process successfully
        Map<String, Object> res1 = inboundWebhookService.handleInboundEvent(
            tenantId, IntegrationProvider.STRIPE, extEventId, "payment_intent.succeeded", payload);
        assertEquals("PROCESSED", res1.get("status"));
        assertNotNull(res1.get("eventId"));

        // Second delivery with duplicate external ID: should be IGNORED (idempotent)
        Map<String, Object> res2 = inboundWebhookService.handleInboundEvent(
            tenantId, IntegrationProvider.STRIPE, extEventId, "payment_intent.succeeded", payload);
        assertEquals("IGNORED", res2.get("status"));
    }
}
