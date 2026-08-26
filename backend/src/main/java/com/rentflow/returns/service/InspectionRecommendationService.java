package com.rentflow.returns.service;

import com.rentflow.returns.model.DamageCategory;
import com.rentflow.returns.model.DamageSeverity;
import com.rentflow.returns.model.InspectionCondition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InspectionRecommendationService {

    public AIInspectionRecommendation recommendCondition(String itemNotes, String productName) {
        AIInspectionRecommendation rec = new AIInspectionRecommendation();
        if (itemNotes != null && (itemNotes.toLowerCase().contains("broken") || itemNotes.toLowerCase().contains("crack"))) {
            rec.setSuggestedCondition(InspectionCondition.UNUSABLE);
            rec.setSuggestedCategory(DamageCategory.BROKEN);
            rec.setSuggestedSeverity(DamageSeverity.CRITICAL);
            rec.setReasoning("AI detected structural damage keywords ('broken'/'crack') in inspection input.");
            rec.setEstimatedRepairCost(BigDecimal.valueOf(50.00));
        } else if (itemNotes != null && (itemNotes.toLowerCase().contains("scratch") || itemNotes.toLowerCase().contains("stain"))) {
            rec.setSuggestedCondition(InspectionCondition.MINOR_DAMAGE);
            rec.setSuggestedCategory(itemNotes.toLowerCase().contains("stain") ? DamageCategory.STAINED : DamageCategory.SCRATCHED);
            rec.setSuggestedSeverity(DamageSeverity.MINOR);
            rec.setReasoning("AI detected minor surface wear ('scratch'/'stain') in inspection input.");
            rec.setEstimatedRepairCost(BigDecimal.valueOf(15.00));
        } else {
            rec.setSuggestedCondition(InspectionCondition.GOOD);
            rec.setSuggestedCategory(DamageCategory.OTHER);
            rec.setSuggestedSeverity(DamageSeverity.MINOR);
            rec.setReasoning("AI detected no reported anomalies. Condition likely GOOD.");
            rec.setEstimatedRepairCost(BigDecimal.ZERO);
        }
        return rec;
    }

    public static class AIInspectionRecommendation {
        private InspectionCondition suggestedCondition;
        private DamageCategory suggestedCategory;
        private DamageSeverity suggestedSeverity;
        private String reasoning;
        private BigDecimal estimatedRepairCost;

        public InspectionCondition getSuggestedCondition() { return suggestedCondition; }
        public void setSuggestedCondition(InspectionCondition suggestedCondition) { this.suggestedCondition = suggestedCondition; }

        public DamageCategory getSuggestedCategory() { return suggestedCategory; }
        public void setSuggestedCategory(DamageCategory suggestedCategory) { this.suggestedCategory = suggestedCategory; }

        public DamageSeverity getSuggestedSeverity() { return suggestedSeverity; }
        public void setSuggestedSeverity(DamageSeverity suggestedSeverity) { this.suggestedSeverity = suggestedSeverity; }

        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }

        public BigDecimal getEstimatedRepairCost() { return estimatedRepairCost; }
        public void setEstimatedRepairCost(BigDecimal estimatedRepairCost) { this.estimatedRepairCost = estimatedRepairCost; }
    }
}
