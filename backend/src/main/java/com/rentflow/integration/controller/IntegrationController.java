package com.rentflow.integration.controller;

import com.rentflow.integration.dto.*;
import com.rentflow.integration.model.IntegrationEvent;
import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.service.IntegrationService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping
    public List<IntegrationConnectionDTO> getConnections() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getConnections(tenantId);
    }

    @PostMapping
    public ResponseEntity<IntegrationConnectionDTO> connect(@RequestBody ConnectIntegrationDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String user = SecurityUtils.getCurrentUser();
        IntegrationConnectionDTO result = integrationService.connect(tenantId, dto, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntegrationConnectionDTO> getConnection(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getConnection(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        boolean success = integrationService.testConnection(tenantId, id);
        return ResponseEntity.ok(Map.of("success", success, "message", success ? "Connection successful" : "Connection failed"));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<IntegrationSyncJobDTO> syncNow(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        IntegrationSyncJobDTO job = integrationService.triggerSync(tenantId, id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{id}/disconnect")
    public ResponseEntity<IntegrationConnectionDTO> disconnect(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.disconnect(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/sync-jobs")
    public List<IntegrationSyncJobDTO> getSyncJobs(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getSyncJobs(tenantId, id);
    }

    @GetMapping("/events")
    public List<IntegrationEvent> getEvents(@RequestParam(defaultValue = "50") int limit) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getEvents(tenantId, limit);
    }

    @GetMapping("/dead-letter")
    public List<WebhookDeliveryDTO> getDeadLetterDeliveries() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getDeadLetterDeliveries(tenantId);
    }

    @GetMapping("/dashboard-summary")
    public IntegrationDashboardSummaryDTO getDashboardSummary() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getDashboardSummary(tenantId);
    }

    @GetMapping("/mappings")
    public List<ExternalEntityMappingDTO> getMappings(@RequestParam(required = false) IntegrationProvider provider) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return integrationService.getEntityMappings(tenantId, provider);
    }
}
