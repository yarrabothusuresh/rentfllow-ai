package com.rentflow.integration.controller;

import com.rentflow.integration.dto.CreateApiKeyDTO;
import com.rentflow.integration.dto.ExternalApiKeyDTO;
import com.rentflow.integration.dto.GeneratedApiKeyResponseDTO;
import com.rentflow.integration.service.ExternalApiKeyService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/api-keys")
public class ExternalApiKeyController {

    private final ExternalApiKeyService apiKeyService;

    public ExternalApiKeyController(ExternalApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public List<ExternalApiKeyDTO> getApiKeys() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return apiKeyService.getApiKeys(tenantId);
    }

    @PostMapping
    public ResponseEntity<GeneratedApiKeyResponseDTO> generateApiKey(@RequestBody CreateApiKeyDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String user = SecurityUtils.getCurrentUser();
        GeneratedApiKeyResponseDTO result = apiKeyService.generateApiKey(tenantId, dto, user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<ExternalApiKeyDTO> revokeApiKey(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return apiKeyService.revokeApiKey(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
