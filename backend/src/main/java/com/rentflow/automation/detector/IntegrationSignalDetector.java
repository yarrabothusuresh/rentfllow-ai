package com.rentflow.automation.detector;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import com.rentflow.integration.model.IntegrationEventStatus;
import com.rentflow.integration.model.IntegrationOutbox;
import com.rentflow.integration.repository.IntegrationOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IntegrationSignalDetector implements BusinessSignalDetector {

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.INTEGRATION_DEAD_LETTER_EVENT
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();

        List<IntegrationOutbox> deadLetters = outboxRepository.findByTenantIdAndStatus(tenantId, IntegrationEventStatus.DEAD_LETTER);
        for (IntegrationOutbox event : deadLetters) {
            Map<String, Object> evidence = new LinkedHashMap<>();
            evidence.put("eventId", event.getEventId());
            evidence.put("eventType", event.getEventType());
            evidence.put("aggregateType", event.getAggregateType());
            evidence.put("aggregateId", event.getAggregateId());
            evidence.put("attempts", event.getAttempts());
            evidence.put("lastError", event.getLastError());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("outboxId", event.getId().toString());
            payload.put("eventId", event.getEventId());
            payload.put("eventType", event.getEventType());

            signals.add(new DetectedSignal(
                BusinessSignalType.INTEGRATION_DEAD_LETTER_EVENT,
                BusinessSignalCategory.INTEGRATION,
                "INTEGRATION_EVENT",
                event.getId().toString(),
                event.getEventId(),
                BusinessSignalSeverity.HIGH,
                tenantId + ":INTEGRATION_DEAD_LETTER:" + event.getId(),
                evidence,
                AutomationActionType.RETRY_INTEGRATION_EVENT,
                payload,
                "Integration event " + event.getEventId() + " in dead-letter queue",
                "Outbound sync event " + event.getEventType() + " for " + event.getAggregateType() + " (" + event.getAggregateId() + ") exhausted " + event.getAttempts() + " retries.",
                "Dead lettered events cause data divergence between RentFlow and external ERP/accounting/CRM systems."
            ));
        }

        return signals;
    }
}
