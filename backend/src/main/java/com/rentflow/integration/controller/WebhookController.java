package com.rentflow.integration.controller;

import com.rentflow.integration.dto.CreateWebhookDTO;
import com.rentflow.integration.dto.WebhookDeliveryDTO;
import com.rentflow.integration.dto.WebhookEndpointDTO;
import com.rentflow.integration.service.WebhookService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping("/webhooks")
    public List<WebhookEndpointDTO> getWebhooks() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return webhookService.getEndpoints(tenantId);
    }

    @PostMapping("/webhooks")
    public ResponseEntity<Map<String, Object>> createWebhook(@RequestBody CreateWebhookDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String user = SecurityUtils.getCurrentUser();
        Map<String, Object> result = webhookService.createEndpoint(tenantId, dto, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/webhooks/{id}")
    public ResponseEntity<WebhookEndpointDTO> getWebhook(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return webhookService.getEndpoint(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/webhooks/{id}")
    public ResponseEntity<WebhookEndpointDTO> updateWebhook(@PathVariable UUID id, @RequestBody CreateWebhookDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return webhookService.updateEndpoint(tenantId, id, dto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/webhooks/{id}")
    public ResponseEntity<Map<String, Object>> deleteWebhook(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        boolean deleted = webhookService.deleteEndpoint(tenantId, id);
        return ResponseEntity.ok(Map.of("success", deleted));
    }

    @PostMapping("/webhooks/{id}/test")
    public ResponseEntity<WebhookDeliveryDTO> testWebhook(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        WebhookDeliveryDTO delivery = webhookService.testWebhook(tenantId, id);
        return ResponseEntity.ok(delivery);
    }

    @PostMapping("/webhooks/{id}/rotate-secret")
    public ResponseEntity<Map<String, Object>> rotateSecret(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return webhookService.rotateSecret(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/webhooks/{id}/deliveries")
    public List<WebhookDeliveryDTO> getDeliveries(@PathVariable UUID id, @RequestParam(defaultValue = "50") int limit) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return webhookService.getDeliveries(tenantId, id, limit);
    }

    @PostMapping("/webhook-deliveries/{id}/retry")
    public ResponseEntity<WebhookDeliveryDTO> retryDelivery(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        WebhookDeliveryDTO newAttempt = webhookService.retryDelivery(tenantId, id);
        return ResponseEntity.ok(newAttempt);
    }
}
