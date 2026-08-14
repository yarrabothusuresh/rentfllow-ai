package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FutureActionTool implements AITool {

    private final String actionName;

    public FutureActionTool() {
        this.actionName = "sendPaymentReminder";
    }

    public FutureActionTool(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String getName() {
        return actionName;
    }

    @Override
    public String getDescription() {
        return "Execute controlled business action: " + actionName;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String actionToPerform = request.getToolName() != null ? request.getToolName() : actionName;

        Map<String, Object> details = new HashMap<>();
        details.put("action", actionToPerform);
        details.put("status", "ACTION_REQUIRES_APPROVAL");
        details.put("requiresApproval", true);

        if ("sendPaymentReminder".equalsIgnoreCase(actionToPerform) || actionToPerform.toLowerCase().contains("remind")) {
            details.put("targetAction", "Send payment reminder");
            details.put("customer", "Emily Brown");
            details.put("outstandingAmount", "$2,500.00");
            details.put("previewMessage", "Dear Emily, your deposit/payment balance of $2,500 is due for your upcoming wedding rental on Sept 20, 2026.");
        } else if ("createQuote".equalsIgnoreCase(actionToPerform)) {
            details.put("targetAction", "Create new quote");
            details.put("customer", "Emily Brown");
            details.put("estimatedTotal", "$6,000.00");
            details.put("previewMessage", "Create quote #Q-8493 for 250-person wedding rental.");
        } else if ("sendQuote".equalsIgnoreCase(actionToPerform)) {
            details.put("targetAction", "Send quote to customer");
            details.put("customer", "Emily Brown");
            details.put("quoteId", "Q-8492");
            details.put("previewMessage", "Send quote #Q-8492 ($6,480) via email to emily.brown@example.com.");
        } else {
            details.put("targetAction", actionToPerform);
            details.put("customer", "Emily Brown");
            details.put("previewMessage", "Perform controlled operational action: " + actionToPerform);
        }

        return ToolResult.requiresApproval(
            details,
            "AI wants to perform sensitive action: " + details.get("targetAction") + ". Explicit user approval required."
        );
    }
}
