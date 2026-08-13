package com.rentflow.workflow.dto;

import com.rentflow.workflow.model.WorkflowStage;

public class WorkflowStageDTO {
    private String name;
    private WorkflowStage stageKey;
    private String status; // COMPLETED, CURRENT, PENDING
    private String role;
    private String description;
    private String date;

    public WorkflowStageDTO() {}

    public WorkflowStageDTO(String name, WorkflowStage stageKey, String status, String role, String description, String date) {
        this.name = name;
        this.stageKey = stageKey;
        this.status = status;
        this.role = role;
        this.description = description;
        this.date = date;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WorkflowStage getStageKey() { return stageKey; }
    public void setStageKey(WorkflowStage stageKey) { this.stageKey = stageKey; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
