package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import java.util.Map;

public interface AutomationActionHandler {
    AutomationActionType getActionType();
    RevalidationResult revalidate(String tenantId, Map<String, Object> payload);
    ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy);
}
