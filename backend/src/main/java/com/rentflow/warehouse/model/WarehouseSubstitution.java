package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouse_substitutions", indexes = {
    @Index(name = "idx_wh_sub_tenant", columnList = "tenantId"),
    @Index(name = "idx_wh_sub_order", columnList = "warehouseOrderId"),
    @Index(name = "idx_wh_sub_status", columnList = "status")
})
public class WarehouseSubstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    @Column(nullable = false)
    private UUID originalProductId;

    @Column(nullable = false)
    private UUID replacementProductId;

    private String originalProductNameSnapshot;
    private String replacementProductNameSnapshot;

    @Column(nullable = false)
    private int originalQuantity;

    @Column(nullable = false)
    private int replacementQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubstitutionStatus status = SubstitutionStatus.PROPOSED;

    @Column(length = 1000)
    private String reason;

    private String proposedBy;
    private String approvedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = SubstitutionStatus.PROPOSED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public UUID getOriginalProductId() { return originalProductId; }
    public void setOriginalProductId(UUID originalProductId) { this.originalProductId = originalProductId; }

    public UUID getReplacementProductId() { return replacementProductId; }
    public void setReplacementProductId(UUID replacementProductId) { this.replacementProductId = replacementProductId; }

    public String getOriginalProductNameSnapshot() { return originalProductNameSnapshot; }
    public void setOriginalProductNameSnapshot(String originalProductNameSnapshot) { this.originalProductNameSnapshot = originalProductNameSnapshot; }

    public String getReplacementProductNameSnapshot() { return replacementProductNameSnapshot; }
    public void setReplacementProductNameSnapshot(String replacementProductNameSnapshot) { this.replacementProductNameSnapshot = replacementProductNameSnapshot; }

    public int getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(int originalQuantity) { this.originalQuantity = originalQuantity; }

    public int getReplacementQuantity() { return replacementQuantity; }
    public void setReplacementQuantity(int replacementQuantity) { this.replacementQuantity = replacementQuantity; }

    public SubstitutionStatus getStatus() { return status; }
    public void setStatus(SubstitutionStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getProposedBy() { return proposedBy; }
    public void setProposedBy(String proposedBy) { this.proposedBy = proposedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
