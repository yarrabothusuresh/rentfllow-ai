package com.rentflow.ai.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AIResponse {
    private String message;
    private String intent;
    private List<String> toolsUsed = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private boolean requiresApproval;
    private Map<String, Object> actionDetails;
    private List<String> reasoningSteps = new ArrayList<>();

    public AIResponse() {}

    public AIResponse(String message, String intent, List<String> toolsUsed, List<String> suggestedActions, boolean requiresApproval) {
        this.message = message;
        this.intent = intent;
        this.toolsUsed = toolsUsed;
        this.suggestedActions = suggestedActions;
        this.requiresApproval = requiresApproval;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public List<String> getToolsUsed() {
        return toolsUsed;
    }

    public void setToolsUsed(List<String> toolsUsed) {
        this.toolsUsed = toolsUsed;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Map<String, Object> getActionDetails() {
        return actionDetails;
    }

    public void setActionDetails(Map<String, Object> actionDetails) {
        this.actionDetails = actionDetails;
    }

    public List<String> getReasoningSteps() {
        return reasoningSteps;
    }

    public void setReasoningSteps(List<String> reasoningSteps) {
        this.reasoningSteps = reasoningSteps;
    }
}
