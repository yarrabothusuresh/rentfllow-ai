package com.rentflow.integration.service;

import com.rentflow.integration.dto.CreateWebhookDTO;
import com.rentflow.integration.dto.WebhookDeliveryDTO;
import com.rentflow.integration.dto.WebhookEndpointDTO;
import com.rentflow.integration.model.DeliveryStatus;
import com.rentflow.integration.model.WebhookDelivery;
import com.rentflow.integration.model.WebhookEndpoint;
import com.rentflow.integration.model.WebhookStatus;
import com.rentflow.integration.repository.WebhookDeliveryRepository;
import com.rentflow.integration.repository.WebhookEndpointRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebhookService {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookDeliveryService deliveryService;
    private final IntegrationCredentialService credentialService;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookService(WebhookEndpointRepository endpointRepository,
                          WebhookDeliveryRepository deliveryRepository,
                          WebhookDeliveryService deliveryService,
                          IntegrationCredentialService credentialService) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryService = deliveryService;
        this.credentialService = credentialService;
    }

    public List<WebhookEndpointDTO> getEndpoints(String tenantId) {
        List<WebhookEndpoint> endpoints = endpointRepository.findByTenantId(tenantId);
        return endpoints.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Optional<WebhookEndpointDTO> getEndpoint(String tenantId, UUID id) {
        return endpointRepository.findByTenantIdAndId(tenantId, id).map(this::mapToDTO);
    }

    @Transactional
    public Map<String, Object> createEndpoint(String tenantId, CreateWebhookDTO dto, String createdBy) {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setTenantId(tenantId);
        endpoint.setName(dto.getName());
        endpoint.setEndpointUrl(dto.getEndpointUrl());
        endpoint.setDescription(dto.getDescription());
        endpoint.setStatus(WebhookStatus.ACTIVE);
        endpoint.setCreatedBy(createdBy);

        if (dto.getSubscribedEvents() == null || dto.getSubscribedEvents().isEmpty()) {
            endpoint.setSubscribedEvents("*");
        } else {
            endpoint.setSubscribedEvents(String.join(",", dto.getSubscribedEvents()));
        }

        // Generate high entropy signing secret (e.g. whsec_...)
        String rawSecret = "whsec_" + generateRandomHex(24);
        endpoint.setSecretReference(credentialService.encryptCredential(rawSecret));

        WebhookEndpoint saved = endpointRepository.save(endpoint);

        Map<String, Object> response = new HashMap<>();
        response.put("endpoint", mapToDTO(saved));
        response.put("signingSecret", rawSecret); // Returned once on creation
        return response;
    }

    @Transactional
    public Optional<WebhookEndpointDTO> updateEndpoint(String tenantId, UUID id, CreateWebhookDTO dto) {
        return endpointRepository.findByTenantIdAndId(tenantId, id).map(endpoint -> {
            if (dto.getName() != null) endpoint.setName(dto.getName());
            if (dto.getEndpointUrl() != null) endpoint.setEndpointUrl(dto.getEndpointUrl());
            if (dto.getDescription() != null) endpoint.setDescription(dto.getDescription());
            if (dto.getSubscribedEvents() != null && !dto.getSubscribedEvents().isEmpty()) {
                endpoint.setSubscribedEvents(String.join(",", dto.getSubscribedEvents()));
            }
            return mapToDTO(endpointRepository.save(endpoint));
        });
    }

    @Transactional
    public boolean deleteEndpoint(String tenantId, UUID id) {
        return endpointRepository.findByTenantIdAndId(tenantId, id).map(endpoint -> {
            endpointRepository.delete(endpoint);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<Map<String, Object>> rotateSecret(String tenantId, UUID id) {
        return endpointRepository.findByTenantIdAndId(tenantId, id).map(endpoint -> {
            String newSecret = "whsec_" + generateRandomHex(24);
            endpoint.setSecretReference(credentialService.encryptCredential(newSecret));
            WebhookEndpoint saved = endpointRepository.save(endpoint);

            Map<String, Object> res = new HashMap<>();
            res.put("endpoint", mapToDTO(saved));
            res.put("newSigningSecret", newSecret);
            return res;
        });
    }

    public WebhookDeliveryDTO testWebhook(String tenantId, UUID endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findByTenantIdAndId(tenantId, endpointId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook endpoint not found"));

        String eventId = "evt_test_" + UUID.randomUUID().toString().replace("-", "");
        String payload = "{\"eventId\":\"" + eventId + "\",\"eventType\":\"webhook.test\",\"eventVersion\":\"1.0\",\"tenantId\":\"" +
                         tenantId + "\",\"occurredAt\":\"" + LocalDateTime.now() + "\",\"data\":{\"message\":\"RentFlow Webhook Connection Test\"}}";

        WebhookDelivery delivery = deliveryService.dispatchWebhook(endpoint, eventId, "webhook.test", payload, 1);
        return mapDeliveryToDTO(delivery, endpoint.getName(), endpoint.getEndpointUrl());
    }

    public List<WebhookDeliveryDTO> getDeliveries(String tenantId, UUID endpointId, int limit) {
        WebhookEndpoint endpoint = endpointRepository.findByTenantIdAndId(tenantId, endpointId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook endpoint not found"));

        List<WebhookDelivery> deliveries = deliveryRepository.findByTenantIdAndWebhookEndpointIdOrderByCreatedAtDesc(tenantId, endpointId);
        return deliveries.stream()
            .limit(limit > 0 ? limit : 50)
            .map(d -> mapDeliveryToDTO(d, endpoint.getName(), endpoint.getEndpointUrl()))
            .collect(Collectors.toList());
    }

    @Transactional
    public WebhookDeliveryDTO retryDelivery(String tenantId, UUID deliveryId) {
        WebhookDelivery oldDelivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        WebhookEndpoint endpoint = endpointRepository.findByTenantIdAndId(tenantId, oldDelivery.getWebhookEndpointId())
            .orElseThrow(() -> new IllegalArgumentException("Webhook endpoint not found"));

        // Manual retry creates next attempt number
        int nextAttempt = oldDelivery.getAttemptNumber() + 1;
        WebhookDelivery newDelivery = deliveryService.dispatchWebhook(
            endpoint,
            oldDelivery.getIntegrationEventId(),
            oldDelivery.getEventType(),
            oldDelivery.getRequestPayload(),
            nextAttempt
        );

        return mapDeliveryToDTO(newDelivery, endpoint.getName(), endpoint.getEndpointUrl());
    }

    private WebhookEndpointDTO mapToDTO(WebhookEndpoint endpoint) {
        WebhookEndpointDTO dto = new WebhookEndpointDTO();
        dto.setId(endpoint.getId());
        dto.setTenantId(endpoint.getTenantId());
        dto.setName(endpoint.getName());
        dto.setEndpointUrl(endpoint.getEndpointUrl());
        dto.setStatus(endpoint.getStatus());
        dto.setDescription(endpoint.getDescription());
        dto.setCreatedBy(endpoint.getCreatedBy());
        dto.setCreatedAt(endpoint.getCreatedAt());
        dto.setUpdatedAt(endpoint.getUpdatedAt());

        String rawSecret = credentialService.decryptCredential(endpoint.getSecretReference());
        dto.setMaskedSecret(credentialService.maskCredential(rawSecret));

        if (endpoint.getSubscribedEvents() != null) {
            dto.setSubscribedEvents(Arrays.asList(endpoint.getSubscribedEvents().split(",")));
        } else {
            dto.setSubscribedEvents(Collections.singletonList("*"));
        }

        List<WebhookDelivery> deliveries = deliveryRepository.findByTenantIdAndWebhookEndpointIdOrderByCreatedAtDesc(
            endpoint.getTenantId(), endpoint.getId());
        dto.setTotalDeliveries(deliveries.size());
        long successful = deliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.SUCCESS).count();
        dto.setSuccessfulDeliveries(successful);
        dto.setSuccessRate(deliveries.isEmpty() ? 100.0 : ((double) successful / deliveries.size()) * 100.0);
        if (!deliveries.isEmpty()) {
            dto.setLastDeliveryAt(deliveries.get(0).getCreatedAt());
        }

        return dto;
    }

    public WebhookDeliveryDTO mapDeliveryToDTO(WebhookDelivery d, String webhookName, String endpointUrl) {
        WebhookDeliveryDTO dto = new WebhookDeliveryDTO();
        dto.setId(d.getId());
        dto.setTenantId(d.getTenantId());
        dto.setWebhookEndpointId(d.getWebhookEndpointId());
        dto.setWebhookName(webhookName);
        dto.setEndpointUrl(endpointUrl);
        dto.setIntegrationEventId(d.getIntegrationEventId());
        dto.setEventType(d.getEventType());
        dto.setAttemptNumber(d.getAttemptNumber());
        dto.setStatus(d.getStatus());
        dto.setHttpStatus(d.getHttpStatus());
        dto.setResponseSummary(d.getResponseSummary());
        dto.setRequestHeaders(d.getRequestHeaders());
        dto.setRequestPayload(d.getRequestPayload());
        dto.setDurationMs(d.getDurationMs());
        dto.setNextRetryAt(d.getNextRetryAt());
        dto.setStartedAt(d.getStartedAt());
        dto.setCompletedAt(d.getCompletedAt());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private String generateRandomHex(int byteCount) {
        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
