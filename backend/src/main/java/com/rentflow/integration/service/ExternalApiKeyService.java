package com.rentflow.integration.service;

import com.rentflow.integration.dto.CreateApiKeyDTO;
import com.rentflow.integration.dto.ExternalApiKeyDTO;
import com.rentflow.integration.dto.GeneratedApiKeyResponseDTO;
import com.rentflow.integration.model.ApiKeyStatus;
import com.rentflow.integration.model.ExternalApiKey;
import com.rentflow.integration.repository.ExternalApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExternalApiKeyService {

    private final ExternalApiKeyRepository apiKeyRepository;
    private final IntegrationCredentialService credentialService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ExternalApiKeyService(ExternalApiKeyRepository apiKeyRepository,
                                 IntegrationCredentialService credentialService) {
        this.apiKeyRepository = apiKeyRepository;
        this.credentialService = credentialService;
    }

    public List<ExternalApiKeyDTO> getApiKeys(String tenantId) {
        return apiKeyRepository.findByTenantId(tenantId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public GeneratedApiKeyResponseDTO generateApiKey(String tenantId, CreateApiKeyDTO dto, String createdBy) {
        String randomPart = generateRandomToken(32);
        String rawKey = "rf_live_" + randomPart;
        String prefix = "rf_live_" + randomPart.substring(0, 4) + "...";
        String hash = credentialService.hashSecret(rawKey);

        ExternalApiKey apiKey = new ExternalApiKey();
        apiKey.setTenantId(tenantId);
        apiKey.setName(dto.getName());
        apiKey.setKeyPrefix(prefix);
        apiKey.setKeyHash(hash);
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setCreatedBy(createdBy);
        apiKey.setRateLimitPerMinute(dto.getRateLimitPerMinute() != null ? dto.getRateLimitPerMinute() : 120);
        apiKey.setExpiresAt(dto.getExpiresAt());

        if (dto.getScopes() == null || dto.getScopes().isEmpty()) {
            apiKey.setScopes("products:read,inventory:read");
        } else {
            apiKey.setScopes(String.join(",", dto.getScopes()));
        }

        ExternalApiKey saved = apiKeyRepository.save(apiKey);
        return new GeneratedApiKeyResponseDTO(mapToDTO(saved), rawKey);
    }

    @Transactional
    public Optional<ExternalApiKeyDTO> revokeApiKey(String tenantId, UUID id) {
        return apiKeyRepository.findByTenantIdAndId(tenantId, id).map(key -> {
            key.setStatus(ApiKeyStatus.REVOKED);
            key.setRevokedAt(LocalDateTime.now());
            return mapToDTO(apiKeyRepository.save(key));
        });
    }

    @Transactional
    public Optional<ExternalApiKey> authenticateApiKey(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return Optional.empty();
        }
        String cleanKey = rawApiKey.trim();
        if (cleanKey.startsWith("Bearer ")) {
            cleanKey = cleanKey.substring(7).trim();
        }

        String hash = credentialService.hashSecret(cleanKey);
        Optional<ExternalApiKey> keyOpt = apiKeyRepository.findByKeyHash(hash);

        if (keyOpt.isPresent()) {
            ExternalApiKey key = keyOpt.get();
            if (key.getStatus() == ApiKeyStatus.ACTIVE) {
                if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
                    key.setStatus(ApiKeyStatus.EXPIRED);
                    apiKeyRepository.save(key);
                    return Optional.empty();
                }
                key.setLastUsedAt(LocalDateTime.now());
                apiKeyRepository.save(key);
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    public boolean hasScope(ExternalApiKey key, String requiredScope) {
        if (key == null || key.getScopes() == null) return false;
        for (String scope : key.getScopes().split(",")) {
            if (scope.trim().equalsIgnoreCase(requiredScope) || scope.trim().equalsIgnoreCase("*")) {
                return true;
            }
        }
        return false;
    }

    private ExternalApiKeyDTO mapToDTO(ExternalApiKey key) {
        ExternalApiKeyDTO dto = new ExternalApiKeyDTO();
        dto.setId(key.getId());
        dto.setTenantId(key.getTenantId());
        dto.setName(key.getName());
        dto.setKeyPrefix(key.getKeyPrefix());
        dto.setStatus(key.getStatus());
        dto.setRateLimitPerMinute(key.getRateLimitPerMinute());
        dto.setLastUsedAt(key.getLastUsedAt());
        dto.setExpiresAt(key.getExpiresAt());
        dto.setCreatedBy(key.getCreatedBy());
        dto.setCreatedAt(key.getCreatedAt());
        dto.setRevokedAt(key.getRevokedAt());

        if (key.getScopes() != null) {
            dto.setScopes(Arrays.asList(key.getScopes().split(",")));
        } else {
            dto.setScopes(Collections.emptyList());
        }

        return dto;
    }

    private String generateRandomToken(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
