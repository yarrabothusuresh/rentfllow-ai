package com.rentflow.claims.service;

import com.rentflow.claims.dto.ClaimRecommendationDTO;
import com.rentflow.claims.model.DamageClaimItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ClaimRecommendationService {

    public ClaimRecommendationDTO evaluateItem(DamageClaimItem item) {
        if (item == null) {
            return new ClaimRecommendationDTO("WAIVE", 0.50, "Insufficient item information.");
        }

        BigDecimal repair = item.getUnitRepairCost() != null ? item.getUnitRepairCost() : BigDecimal.ZERO;
        BigDecimal replacement = item.getUnitReplacementCost() != null ? item.getUnitReplacementCost() : BigDecimal.ZERO;

        if (replacement.compareTo(BigDecimal.ZERO) > 0 && repair.compareTo(replacement.multiply(BigDecimal.valueOf(0.70))) > 0) {
            return new ClaimRecommendationDTO("REPLACEMENT", 0.88, "Repair cost exceeds 70% of replacement cost. Replacement recommended.");
        } else if (repair.compareTo(BigDecimal.ZERO) > 0) {
            return new ClaimRecommendationDTO("REPAIR", 0.92, "Historical repair cost is significantly lower than replacement cost.");
        } else {
            return new ClaimRecommendationDTO("REPLACEMENT", 0.80, "No viable repair cost documented. Replacement recommended.");
        }
    }
}
