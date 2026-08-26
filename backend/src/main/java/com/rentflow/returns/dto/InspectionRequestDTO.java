package com.rentflow.returns.dto;

import com.rentflow.returns.model.DamageCategory;
import com.rentflow.returns.model.DamageSeverity;
import com.rentflow.returns.model.InspectionCondition;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class InspectionRequestDTO {

    private List<ItemInspection> items;

    public List<ItemInspection> getItems() { return items; }
    public void setItems(List<ItemInspection> items) { this.items = items; }

    public static class ItemInspection {
        private UUID returnItemId;
        private int goodQuantity;
        private int damagedQuantity;
        private InspectionCondition condition;
        private String notes;
        private DamageCategory damageCategory;
        private DamageSeverity damageSeverity;
        private String damageDescription;
        private BigDecimal estimatedRepairCost;
        private BigDecimal estimatedReplacementCost;

        public UUID getReturnItemId() { return returnItemId; }
        public void setReturnItemId(UUID returnItemId) { this.returnItemId = returnItemId; }

        public int getGoodQuantity() { return goodQuantity; }
        public void setGoodQuantity(int goodQuantity) { this.goodQuantity = goodQuantity; }

        public int getDamagedQuantity() { return damagedQuantity; }
        public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }

        public InspectionCondition getCondition() { return condition; }
        public void setCondition(InspectionCondition condition) { this.condition = condition; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public DamageCategory getDamageCategory() { return damageCategory; }
        public void setDamageCategory(DamageCategory damageCategory) { this.damageCategory = damageCategory; }

        public DamageSeverity getDamageSeverity() { return damageSeverity; }
        public void setDamageSeverity(DamageSeverity damageSeverity) { this.damageSeverity = damageSeverity; }

        public String getDamageDescription() { return damageDescription; }
        public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }

        public BigDecimal getEstimatedRepairCost() { return estimatedRepairCost; }
        public void setEstimatedRepairCost(BigDecimal estimatedRepairCost) { this.estimatedRepairCost = estimatedRepairCost; }

        public BigDecimal getEstimatedReplacementCost() { return estimatedReplacementCost; }
        public void setEstimatedReplacementCost(BigDecimal estimatedReplacementCost) { this.estimatedReplacementCost = estimatedReplacementCost; }
    }
}
