package com.rentflow.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rentflow.integration.dto.IntegrationEnvelopeDTO;
import com.rentflow.integration.model.IntegrationEvent;
import com.rentflow.integration.model.IntegrationEventStatus;
import com.rentflow.integration.model.IntegrationOutbox;
import com.rentflow.integration.repository.IntegrationEventRepository;
import com.rentflow.integration.repository.IntegrationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class IntegrationEventService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEventService.class);
    private final IntegrationEventRepository eventRepository;
    private final IntegrationOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public IntegrationEventService(IntegrationEventRepository eventRepository,
                                   IntegrationOutboxRepository outboxRepository) {
        this.eventRepository = eventRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public IntegrationEvent publishEvent(String tenantId, String eventType, String aggregateType, String aggregateId, Object data) {
        String eventId = "evt_" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        IntegrationEnvelopeDTO<Object> envelope = new IntegrationEnvelopeDTO<>(
            eventId,
            eventType,
            "1.0",
            tenantId,
            now,
            data
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize integration event payload", e);
            payloadJson = "{\"error\":\"Serialization failure\"}";
        }

        IntegrationEvent event = new IntegrationEvent();
        event.setTenantId(tenantId);
        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setPayload(payloadJson);
        event.setStatus(IntegrationEventStatus.PENDING);
        event.setOccurredAt(now);
        event = eventRepository.save(event);

        // Also write directly to the transactional outbox table
        IntegrationOutbox outbox = new IntegrationOutbox();
        outbox.setTenantId(tenantId);
        outbox.setEventId(eventId);
        outbox.setEventType(eventType);
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setPayload(payloadJson);
        outbox.setStatus(IntegrationEventStatus.PENDING);
        outbox.setAttempts(0);
        outbox.setNextAttemptAt(now);
        outboxRepository.save(outbox);

        log.info("[IntegrationEvent] Event {} published to Outbox with ID {}", eventType, eventId);
        return event;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
