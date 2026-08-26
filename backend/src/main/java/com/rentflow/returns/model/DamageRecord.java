package com.rentflow.returns.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "damage_records", indexes = {
    @Index(name = "idx_damage_tenant", columnList = "tenantId"),
    @Index(name = "idx_damage_inspection", columnList = "inspectionId"),
    @Index(name = "idx_damage_product", columnList = "productId"),
    @Index(name = "idx_damage_return_item", columnList = "returnItemId")
})
public class DamageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    private UUID inspectionId;
    private UUID returnItemId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DamageCategory category = DamageCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DamageSeverity severity = DamageSeverity.MINOR;

    @Column(length = 2000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedRepairCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedReplacementCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "OPEN";

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getInspectionId() { return inspectionId; }
    public void setInspectionId(UUID inspectionId) { this.inspectionId = inspectionId; }

    public UUID getReturnItemId() { return returnItemId; }
    public void setReturnItemId(UUID returnItemId) { this.returnItemId = returnItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public DamageCategory getCategory() { return category; }
    public void setCategory(DamageCategory category) { this.category = category; }

    public DamageSeverity getSeverity() { return severity; }
    public void setSeverity(DamageSeverity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getEstimatedRepairCost() { return estimatedRepairCost; }
    public void setEstimatedRepairCost(BigDecimal estimatedRepairCost) { this.estimatedRepairCost = estimatedRepairCost; }

    public BigDecimal getEstimatedReplacementCost() { return estimatedReplacementCost; }
    public void setEstimatedReplacementCost(BigDecimal estimatedReplacementCost) { this.estimatedReplacementCost = estimatedReplacementCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
