package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pick_verifications", indexes = {
    @Index(name = "idx_pick_verif_tenant", columnList = "tenantId"),
    @Index(name = "idx_pick_verif_list", columnList = "pickListId"),
    @Index(name = "idx_pick_verif_order", columnList = "warehouseOrderId")
})
public class PickVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID pickListId;

    @Column(nullable = false)
    private UUID warehouseOrderId;

    @Column(nullable = false)
    private String verifiedBy;

    private boolean allVerified = true;

    @Column(length = 2000)
    private String exceptionAcknowledgements;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        if (verifiedAt == null) verifiedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getPickListId() { return pickListId; }
    public void setPickListId(UUID pickListId) { this.pickListId = pickListId; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public boolean isAllVerified() { return allVerified; }
    public void setAllVerified(boolean allVerified) { this.allVerified = allVerified; }

    public String getExceptionAcknowledgements() { return exceptionAcknowledgements; }
    public void setExceptionAcknowledgements(String exceptionAcknowledgements) { this.exceptionAcknowledgements = exceptionAcknowledgements; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
