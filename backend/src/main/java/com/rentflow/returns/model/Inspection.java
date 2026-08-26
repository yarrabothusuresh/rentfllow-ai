package com.rentflow.returns.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inspections", indexes = {
    @Index(name = "idx_inspection_tenant", columnList = "tenantId"),
    @Index(name = "idx_inspection_order", columnList = "returnOrderId"),
    @Index(name = "idx_inspection_item", columnList = "returnOrderItemId"),
    @Index(name = "idx_inspection_product", columnList = "productId")
})
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID returnOrderId;

    @Column(nullable = false)
    private UUID returnOrderItemId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int inspectedQuantity = 0;

    @Column(nullable = false)
    private int goodQuantity = 0;

    @Column(nullable = false)
    private int damagedQuantity = 0;

    @Column(nullable = false)
    private int missingQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionCondition condition = InspectionCondition.GOOD;

    @Column(length = 2000)
    private String notes;

    private String inspectedBy;
    private LocalDateTime inspectedAt;

    @PrePersist
    protected void onCreate() {
        if (inspectedAt == null) inspectedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getReturnOrderId() { return returnOrderId; }
    public void setReturnOrderId(UUID returnOrderId) { this.returnOrderId = returnOrderId; }

    public UUID getReturnOrderItemId() { return returnOrderItemId; }
    public void setReturnOrderItemId(UUID returnOrderItemId) { this.returnOrderItemId = returnOrderItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getInspectedQuantity() { return inspectedQuantity; }
    public void setInspectedQuantity(int inspectedQuantity) { this.inspectedQuantity = inspectedQuantity; }

    public int getGoodQuantity() { return goodQuantity; }
    public void setGoodQuantity(int goodQuantity) { this.goodQuantity = goodQuantity; }

    public int getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }

    public int getMissingQuantity() { return missingQuantity; }
    public void setMissingQuantity(int missingQuantity) { this.missingQuantity = missingQuantity; }

    public InspectionCondition getCondition() { return condition; }
    public void setCondition(InspectionCondition condition) { this.condition = condition; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInspectedBy() { return inspectedBy; }
    public void setInspectedBy(String inspectedBy) { this.inspectedBy = inspectedBy; }

    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }
}
