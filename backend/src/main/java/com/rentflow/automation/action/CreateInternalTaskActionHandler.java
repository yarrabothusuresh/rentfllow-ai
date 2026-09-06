package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateInternalTaskActionHandler implements AutomationActionHandler {

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.CREATE_INTERNAL_TASK;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("taskTitle")) {
            return RevalidationResult.stale("Missing task title in action payload");
        }
        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        String title = (String) payload.get("taskTitle");
        String role = (String) payload.getOrDefault("assignedRole", "ROLE_ADMIN");
        String priority = (String) payload.getOrDefault("priority", "HIGH");
        String entityType = (String) payload.getOrDefault("entityType", "SYSTEM");
        String entityId = (String) payload.getOrDefault("entityId", "");

        return ActionResult.success(
            "Created internal task: '" + title + "' (assigned to " + role + ", priority " + priority + ") for " + entityType + " #" + entityId,
            Map.of("taskTitle", title, "assignedRole", role, "status", "OPEN")
        );
    }
}
