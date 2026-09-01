package com.rentflow.integration.controller;

import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.service.InboundWebhookService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/inbound")
public class InboundWebhookController {

    private final InboundWebhookService inboundService;

    public InboundWebhookController(InboundWebhookService inboundService) {
        this.inboundService = inboundService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @PathVariable String provider,
            @RequestHeader(value = "X-External-Event-Id", required = false) String headerEventId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenantId,
            @RequestBody String payload) {

        IntegrationProvider intProvider;
        try {
            intProvider = IntegrationProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            intProvider = IntegrationProvider.CUSTOM_WEBHOOK;
        }

        String tenantId = (headerTenantId != null && !headerTenantId.isBlank())
            ? headerTenantId
            : SecurityUtils.getCurrentTenantId();

        String eventId = (headerEventId != null && !headerEventId.isBlank())
            ? headerEventId
            : "inb_" + UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> result = inboundService.handleInboundEvent(tenantId, intProvider, eventId, "inbound.event", payload);
        return ResponseEntity.ok(result);
    }
}
