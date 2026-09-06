package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.integration.model.IntegrationEventStatus;
import com.rentflow.integration.model.IntegrationOutbox;
import com.rentflow.integration.repository.IntegrationOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class RetryIntegrationEventActionHandler implements AutomationActionHandler {

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.RETRY_INTEGRATION_EVENT;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("outboxId")) {
            return RevalidationResult.stale("Missing outboxId in payload");
        }
        UUID outboxId;
        try {
            outboxId = UUID.fromString(payload.get("outboxId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid outboxId: " + payload.get("outboxId"));
        }

        IntegrationOutbox event = outboxRepository.findById(outboxId).orElse(null);
        if (event == null || !tenantId.equals(event.getTenantId())) {
            return RevalidationResult.stale("Outbox event not found or belongs to another tenant");
        }

        if (event.getStatus() == IntegrationEventStatus.COMPLETED) {
            return RevalidationResult.alreadyCompleted("Integration event " + event.getEventId() + " has already succeeded");
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID outboxId = UUID.fromString(payload.get("outboxId").toString());
        IntegrationOutbox event = outboxRepository.findById(outboxId).orElseThrow();

        event.setStatus(IntegrationEventStatus.PENDING);
        event.setNextAttemptAt(LocalDateTime.now());
        event.setLastError("Retried via Automation Engine by " + executedBy);
        outboxRepository.save(event);

        return ActionResult.success(
            "Queued integration event " + event.getEventId() + " (" + event.getEventType() + ") for immediate retry",
            Map.of("eventId", event.getEventId(), "status", "PENDING", "nextAttemptAt", LocalDateTime.now().toString())
        );
    }
}
