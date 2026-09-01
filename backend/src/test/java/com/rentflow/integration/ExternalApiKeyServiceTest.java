package com.rentflow.integration;

import com.rentflow.integration.dto.CreateApiKeyDTO;
import com.rentflow.integration.dto.ExternalApiKeyDTO;
import com.rentflow.integration.dto.GeneratedApiKeyResponseDTO;
import com.rentflow.integration.model.ApiKeyStatus;
import com.rentflow.integration.model.ExternalApiKey;
import com.rentflow.integration.service.ExternalApiKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExternalApiKeyServiceTest {

    @Autowired
    private ExternalApiKeyService apiKeyService;

    private final String tenantId = "test-tenant-apikeys";

    @Test
    public void testGenerateApiKeyAndHashStorage() {
        CreateApiKeyDTO dto = new CreateApiKeyDTO();
        dto.setName("Mobile App Key");
        dto.setScopes(List.of("products:read", "inventory:read"));
        dto.setRateLimitPerMinute(100);

        GeneratedApiKeyResponseDTO resp = apiKeyService.generateApiKey(tenantId, dto, "Admin");

        assertNotNull(resp.getRawApiKey());
        assertTrue(resp.getRawApiKey().startsWith("rf_live_"));
        assertNotNull(resp.getApiKey());
        assertEquals("Mobile App Key", resp.getApiKey().getName());
        assertEquals(ApiKeyStatus.ACTIVE, resp.getApiKey().getStatus());

        // Test authentication with raw key
        Optional<ExternalApiKey> auth = apiKeyService.authenticateApiKey(resp.getRawApiKey());
        assertTrue(auth.isPresent());
        assertEquals(tenantId, auth.get().getTenantId());

        // Test scope check
        assertTrue(apiKeyService.hasScope(auth.get(), "products:read"));
        assertTrue(apiKeyService.hasScope(auth.get(), "inventory:read"));
        assertFalse(apiKeyService.hasScope(auth.get(), "bookings:write"));
    }

    @Test
    public void testRevokeApiKey() {
        CreateApiKeyDTO dto = new CreateApiKeyDTO();
        dto.setName("Temporary Key");
        GeneratedApiKeyResponseDTO resp = apiKeyService.generateApiKey(tenantId, dto, "Admin");

        var revoked = apiKeyService.revokeApiKey(tenantId, resp.getApiKey().getId());
        assertTrue(revoked.isPresent());
        assertEquals(ApiKeyStatus.REVOKED, revoked.get().getStatus());

        // Authenticating revoked key should fail
        Optional<ExternalApiKey> auth = apiKeyService.authenticateApiKey(resp.getRawApiKey());
        assertTrue(auth.isEmpty());
    }

    @Test
    public void testExpiredApiKey() {
        CreateApiKeyDTO dto = new CreateApiKeyDTO();
        dto.setName("Expired Key");
        dto.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        GeneratedApiKeyResponseDTO resp = apiKeyService.generateApiKey(tenantId, dto, "Admin");

        Optional<ExternalApiKey> auth = apiKeyService.authenticateApiKey(resp.getRawApiKey());
        assertTrue(auth.isEmpty());
    }
}
