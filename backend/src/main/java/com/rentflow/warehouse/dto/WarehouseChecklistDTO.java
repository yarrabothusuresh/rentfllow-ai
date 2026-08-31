package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.ChecklistStage;
import java.time.LocalDateTime;
import java.util.UUID;

public class WarehouseChecklistDTO {
    private UUID id;
    private UUID warehouseOrderId;
    private ChecklistStage stage;
    private String taskDescription;
    private boolean mandatory;
    private boolean completed;
    private String completedBy;
    private LocalDateTime completedAt;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public ChecklistStage getStage() { return stage; }
    public void setStage(ChecklistStage stage) { this.stage = stage; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
