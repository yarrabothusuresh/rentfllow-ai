package com.rentflow.workflow.dto;

import com.rentflow.workflow.model.WorkflowStage;

public class WorkflowTransitionResponse {
    private boolean success;
    private String message;
    private WorkflowStage previousStage;
    private WorkflowStage currentStage;
    private int newProgress;
    private String timestamp;

    public WorkflowTransitionResponse() {}

    public WorkflowTransitionResponse(boolean success, String message, WorkflowStage previousStage, WorkflowStage currentStage, int newProgress, String timestamp) {
        this.success = success;
        this.message = message;
        this.previousStage = previousStage;
        this.currentStage = currentStage;
        this.newProgress = newProgress;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public WorkflowStage getPreviousStage() { return previousStage; }
    public void setPreviousStage(WorkflowStage previousStage) { this.previousStage = previousStage; }

    public WorkflowStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(WorkflowStage currentStage) { this.currentStage = currentStage; }

    public int getNewProgress() { return newProgress; }
    public void setNewProgress(int newProgress) { this.newProgress = newProgress; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
