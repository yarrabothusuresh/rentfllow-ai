package com.rentflow.integration.service;

import com.rentflow.integration.connector.IntegrationConnector;
import com.rentflow.integration.model.*;
import com.rentflow.integration.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class IntegrationOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(IntegrationOutboxProcessor.class);

    private final IntegrationOutboxRepository outboxRepository;
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryService deliveryService;
    private final IntegrationConnectionRepository connectionRepository;
    private final List<IntegrationConnector> connectors;

    public IntegrationOutboxProcessor(IntegrationOutboxRepository outboxRepository,
                                     WebhookEndpointRepository webhookEndpointRepository,
                                     WebhookDeliveryService deliveryService,
                                     IntegrationConnectionRepository connectionRepository,
                                     List<IntegrationConnector> connectors) {
        this.outboxRepository = outboxRepository;
        this.webhookEndpointRepository = webhookEndpointRepository;
        this.deliveryService = deliveryService;
        this.connectionRepository = connectionRepository;
        this.connectors = connectors;
    }

    /**
     * Poll pending outbox entries periodically or can be triggered directly
     */
    @Scheduled(fixedDelay = 5000)
    public void processPendingOutboxEvents() {
        List<IntegrationOutbox> pending = outboxRepository.findByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(
            IntegrationEventStatus.PENDING, LocalDateTime.now());

        for (IntegrationOutbox outbox : pending) {
            processSingleOutboxEvent(outbox);
        }
    }

    @Transactional
    public void processSingleOutboxEvent(IntegrationOutbox outbox) {
        outbox.setStatus(IntegrationEventStatus.PROCESSING);
        outbox.setAttempts(outbox.getAttempts() + 1);
        outboxRepository.save(outbox);

        boolean hasError = false;
        try {
            // 1. Dispatch to Webhooks
            List<WebhookEndpoint> endpoints = webhookEndpointRepository.findByTenantIdAndStatus(
                outbox.getTenantId(), WebhookStatus.ACTIVE);

            for (WebhookEndpoint ep : endpoints) {
                if (isSubscribed(ep, outbox.getEventType())) {
                    deliveryService.dispatchWebhook(ep, outbox.getEventId(), outbox.getEventType(), outbox.getPayload(), outbox.getAttempts());
                }
            }

            // 2. Dispatch to Active Connectors
            List<IntegrationConnection> connected = connectionRepository.findByTenantIdAndStatus(
                outbox.getTenantId(), ConnectionStatus.CONNECTED);

            IntegrationEvent fakeEvent = new IntegrationEvent();
            fakeEvent.setTenantId(outbox.getTenantId());
            fakeEvent.setEventId(outbox.getEventId());
            fakeEvent.setEventType(outbox.getEventType());
            fakeEvent.setAggregateType(outbox.getAggregateType());
            fakeEvent.setAggregateId(outbox.getAggregateId());
            fakeEvent.setPayload(outbox.getPayload());

            for (IntegrationConnection conn : connected) {
                for (IntegrationConnector connector : connectors) {
                    if (connector.provider() == conn.getProvider()) {
                        try {
                            connector.handleEvent(outbox.getTenantId(), fakeEvent);
                        } catch (Exception e) {
                            log.warn("[OutboxProcessor] Connector {} error processing event {}: {}", conn.getProvider(), outbox.getEventId(), e.getMessage());
                        }
                    }
                }
            }

            outbox.setStatus(IntegrationEventStatus.COMPLETED);
            outbox.setProcessedAt(LocalDateTime.now());
            outbox.setLastError(null);

        } catch (Exception e) {
            hasError = true;
            log.error("[OutboxProcessor] Error processing outbox event " + outbox.getEventId(), e);
            if (outbox.getAttempts() >= 5) {
                outbox.setStatus(IntegrationEventStatus.DEAD_LETTER);
            } else {
                outbox.setStatus(IntegrationEventStatus.PENDING);
                outbox.setNextAttemptAt(LocalDateTime.now().plusSeconds(30L * outbox.getAttempts()));
            }
            outbox.setLastError(e.getMessage());
        }

        outboxRepository.save(outbox);
    }

    private boolean isSubscribed(WebhookEndpoint endpoint, String eventType) {
        if (endpoint.getSubscribedEvents() == null || endpoint.getSubscribedEvents().contains("*")) {
            return true;
        }
        for (String sub : endpoint.getSubscribedEvents().split(",")) {
            if (sub.trim().equalsIgnoreCase(eventType) || sub.trim().equalsIgnoreCase(eventType.replace(".", "_"))) {
                return true;
            }
        }
        return false;
    }
}
