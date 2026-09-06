package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateMaintenanceReviewTaskActionHandler implements AutomationActionHandler {

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.CREATE_MAINTENANCE_REVIEW_TASK;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null) {
            return RevalidationResult.stale("Missing maintenance payload");
        }
        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        String assetCode = (String) payload.getOrDefault("assetCode", "ASSET");
        return ActionResult.success(
            "Created maintenance review task for asset " + assetCode,
            Map.of("assetCode", assetCode, "status", "OPEN", "assignedRole", "ROLE_WAREHOUSE")
        );
    }
}
