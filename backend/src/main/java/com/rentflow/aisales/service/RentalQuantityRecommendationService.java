package com.rentflow.aisales.service;

import com.rentflow.ai.model.Product;
import org.springframework.stereotype.Service;

@Service
public class RentalQuantityRecommendationService {

    public static class RecommendationResult {
        private final int recommendedQuantity;
        private final String explanation;
        private final int configuredUnitCapacity;

        public RecommendationResult(int recommendedQuantity, String explanation, int configuredUnitCapacity) {
            this.recommendedQuantity = recommendedQuantity;
            this.explanation = explanation;
            this.configuredUnitCapacity = configuredUnitCapacity;
        }

        public int getRecommendedQuantity() { return recommendedQuantity; }
        public String getExplanation() { return explanation; }
        public int getConfiguredUnitCapacity() { return configuredUnitCapacity; }
    }

    public RecommendationResult recommendQuantity(Product product, int guestCount, String eventType) {
        if (guestCount <= 0) {
            return new RecommendationResult(1, "Default quantity of 1 for unspecified guest count.", 1);
        }

        String name = product != null && product.getName() != null ? product.getName().toLowerCase() : "";

        // Seating tables
        if (name.contains("60-inch round") || name.contains("round table")) {
            int capacity = 10; // 60-inch round table seats 10 guests comfortably
            int tables = (int) Math.ceil((double) guestCount / capacity);
            return new RecommendationResult(tables,
                String.format("%d tables recommended because each 60\" round table seats %d guests.", tables, capacity),
                capacity);
        } else if (name.contains("banquet") || name.contains("rectangular") || name.contains("6-foot") || name.contains("8-foot")) {
            int capacity = 8; // 6-foot / 8-foot banquet table seats 8
            int tables = (int) Math.ceil((double) guestCount / capacity);
            return new RecommendationResult(tables,
                String.format("%d banquet tables recommended because each rectangular table seats %d guests.", tables, capacity),
                capacity);
        } else if (name.contains("chair") || name.contains("seating")) {
            // 1 chair per guest + 5% buffer for weddings/galas
            int buffer = "WEDDING".equalsIgnoreCase(eventType) ? (int) Math.ceil(guestCount * 0.05) : 0;
            int totalChairs = guestCount + buffer;
            String expl = buffer > 0
                ? String.format("%d chairs recommended: %d for guests plus %d courtesy spare chairs for a wedding layout.", totalChairs, guestCount, buffer)
                : String.format("%d chairs recommended (1 chair per attendee).", totalChairs);
            return new RecommendationResult(totalChairs, expl, 1);
        } else if (name.contains("linen") || name.contains("tablecloth")) {
            // 1 linen per table, assuming round table seating by default
            int tables = (int) Math.ceil((double) guestCount / 10.0);
            return new RecommendationResult(tables,
                String.format("%d table linens recommended to cover %d dining tables.", tables, tables),
                10);
        } else if (name.contains("napkin")) {
            int napkins = (int) Math.ceil(guestCount * 1.1); // 10% extra napkins
            return new RecommendationResult(napkins,
                String.format("%d napkins recommended (%d guests with 10%% hospitality reserve).", napkins, guestCount),
                1);
        }

        return new RecommendationResult(1, "Standard single unit recommendation.", 1);
    }
}
