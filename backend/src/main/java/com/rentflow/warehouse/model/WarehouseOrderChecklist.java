package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouse_order_checklists", indexes = {
    @Index(name = "idx_checklist_tenant", columnList = "tenantId"),
    @Index(name = "idx_checklist_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_checklist_stage", columnList = "stage")
})
public class WarehouseOrderChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChecklistStage stage = ChecklistStage.PICK;

    @Column(nullable = false)
    private String taskDescription;

    private boolean mandatory = true;

    private boolean completed = false;

    private String completedBy;

    private LocalDateTime completedAt;

    private String notes;

    public WarehouseOrderChecklist() {}

    public WarehouseOrderChecklist(UUID id, String tenantId, UUID warehouseOrderId, ChecklistStage stage, String taskDescription, boolean mandatory) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.warehouseOrderId = warehouseOrderId;
        this.stage = stage;
        this.taskDescription = taskDescription;
        this.mandatory = mandatory;
        this.completed = false;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

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
