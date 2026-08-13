package com.rentflow.workflow.dto;

import com.rentflow.workflow.model.WorkflowStage;

public class WorkflowTransitionRequest {
    private WorkflowStage targetStage;
    private String reason;
    private String requestedByRole;

    public WorkflowTransitionRequest() {}

    public WorkflowStage getTargetStage() { return targetStage; }
    public void setTargetStage(WorkflowStage targetStage) { this.targetStage = targetStage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRequestedByRole() { return requestedByRole; }
    public void setRequestedByRole(String requestedByRole) { this.requestedByRole = requestedByRole; }
}
