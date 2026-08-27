package com.rentflow.claims.dto;

import com.rentflow.claims.model.ClaimResolution;
import com.rentflow.claims.model.ClaimType;
import com.rentflow.returns.model.DamageCategory;
import com.rentflow.returns.model.DamageSeverity;
import com.rentflow.returns.model.InspectionCondition;

import java.math.BigDecimal;
import java.util.UUID;

public class DamageClaimItemDTO {
    private UUID id;
    private UUID claimId;
    private UUID returnItemId;
    private UUID inspectionId;
    private UUID productId;
    private String productNameSnapshot;
    private String skuSnapshot;
    private int quantity;
    private ClaimType claimType;
    private InspectionCondition condition;
    private DamageCategory damageCategory;
    private DamageSeverity severity;
    private String description;
    private BigDecimal unitRepairCost;
    private BigDecimal unitReplacementCost;
    private BigDecimal estimatedCost;
    private BigDecimal approvedCost;
    private BigDecimal finalCost;
    private ClaimResolution resolution;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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
}
