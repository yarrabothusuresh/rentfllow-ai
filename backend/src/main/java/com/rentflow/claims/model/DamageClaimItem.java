package com.rentflow.claims.model;

import com.rentflow.returns.model.DamageCategory;
import com.rentflow.returns.model.DamageSeverity;
import com.rentflow.returns.model.InspectionCondition;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "damage_claim_items", indexes = {
    @Index(name = "idx_claim_item_tenant", columnList = "tenantId"),
    @Index(name = "idx_claim_item_claim", columnList = "claimId"),
    @Index(name = "idx_claim_item_product", columnList = "productId")
})
public class DamageClaimItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID claimId;

    private UUID returnItemId;
    private UUID inspectionId;

    @Column(nullable = false)
    private UUID productId;

    private String productNameSnapshot;
    private String skuSnapshot;

    @Column(nullable = false)
    private int quantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType = ClaimType.DAMAGE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionCondition condition = InspectionCondition.GOOD;

    @Enumerated(EnumType.STRING)
    private DamageCategory damageCategory = DamageCategory.OTHER;

    @Enumerated(EnumType.STRING)
    private DamageSeverity severity = DamageSeverity.MINOR;

    @Column(length = 2000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitRepairCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitReplacementCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal approvedCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private ClaimResolution resolution;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getClaimId() { return claimId; }
    public void setClaimId(UUID claimId) { this.claimId = claimId; }

    public UUID getReturnItemId() { return returnItemId; }
    public void setReturnItemId(UUID returnItemId) { this.returnItemId = returnItemId; }

    public UUID getInspectionId() { return inspectionId; }
    public void setInspectionId(UUID inspectionId) { this.inspectionId = inspectionId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public ClaimType getClaimType() { return claimType; }
    public void setClaimType(ClaimType claimType) { this.claimType = claimType; }

    public InspectionCondition getCondition() { return condition; }
    public void setCondition(InspectionCondition condition) { this.condition = condition; }

    public DamageCategory getDamageCategory() { return damageCategory; }
    public void setDamageCategory(DamageCategory damageCategory) { this.damageCategory = damageCategory; }

    public DamageSeverity getSeverity() { return severity; }
    public void setSeverity(DamageSeverity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getUnitRepairCost() { return unitRepairCost; }
    public void setUnitRepairCost(BigDecimal unitRepairCost) { this.unitRepairCost = unitRepairCost; }

    public BigDecimal getUnitReplacementCost() { return unitReplacementCost; }
    public void setUnitReplacementCost(BigDecimal unitReplacementCost) { this.unitReplacementCost = unitReplacementCost; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public BigDecimal getApprovedCost() { return approvedCost; }
    public void setApprovedCost(BigDecimal approvedCost) { this.approvedCost = approvedCost; }

    public BigDecimal getFinalCost() { return finalCost; }
    public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }

    public ClaimResolution getResolution() { return resolution; }
    public void setResolution(ClaimResolution resolution) { this.resolution = resolution; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
