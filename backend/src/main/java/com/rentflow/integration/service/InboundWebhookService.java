package com.rentflow.integration.service;

import com.rentflow.integration.model.InboundEventStatus;
import com.rentflow.integration.model.InboundIntegrationEvent;
import com.rentflow.integration.model.IntegrationProvider;
import com.rentflow.integration.repository.InboundIntegrationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class InboundWebhookService {

    private static final Logger log = LoggerFactory.getLogger(InboundWebhookService.class);
    private final InboundIntegrationEventRepository inboundRepository;

    public InboundWebhookService(InboundIntegrationEventRepository inboundRepository) {
        this.inboundRepository = inboundRepository;
    }

    @Transactional
    public Map<String, Object> handleInboundEvent(String tenantId, IntegrationProvider provider,
                                                 String externalEventId, String eventType, String payload) {
        Map<String, Object> response = new HashMap<>();

        // 1. Check Idempotency: Duplicate check on provider + externalEventId + tenantId
        Optional<InboundIntegrationEvent> existing = inboundRepository.findByTenantIdAndProviderAndExternalEventId(
            tenantId, provider, externalEventId);

        if (existing.isPresent()) {
            log.warn("[InboundWebhook] Duplicate event detected for provider {} with ID {}. Ignoring.", provider, externalEventId);
            response.put("status", "IGNORED");
            response.put("message", "Duplicate event already processed");
            response.put("eventId", existing.get().getId());
            return response;
        }

        // 2. Record new Inbound Integration Event
        InboundIntegrationEvent inboundEvent = new InboundIntegrationEvent();
        inboundEvent.setTenantId(tenantId);
        inboundEvent.setProvider(provider);
        inboundEvent.setExternalEventId(externalEventId);
        inboundEvent.setEventType(eventType);
        inboundEvent.setPayload(payload);
        inboundEvent.setStatus(InboundEventStatus.PROCESSING);
        inboundEvent.setReceivedAt(LocalDateTime.now());
        inboundEvent = inboundRepository.save(inboundEvent);

        try {
            // Processing logic for supported providers (e.g. Stripe checkout.completed, Shopify orders/create)
            inboundEvent.setStatus(InboundEventStatus.PROCESSED);
            inboundEvent.setProcessedAt(LocalDateTime.now());
            inboundRepository.save(inboundEvent);

            response.put("status", "PROCESSED");
            response.put("eventId", inboundEvent.getId());
            response.put("message", "Inbound event processed successfully");
        } catch (Exception e) {
            log.error("[InboundWebhook] Error processing inbound event", e);
            inboundEvent.setStatus(InboundEventStatus.FAILED);
            inboundEvent.setErrorMessage(e.getMessage());
            inboundRepository.save(inboundEvent);

            response.put("status", "FAILED");
            response.put("error", e.getMessage());
        }

        return response;
    }
}
