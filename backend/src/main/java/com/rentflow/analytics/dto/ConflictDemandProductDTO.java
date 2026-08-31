package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ConflictDemandProductDTO {
    private UUID productId;
    private String sku;
    private String name;
    private String categoryName;
    private int conflictIncidentsCount = 0;
    private int unmetQuantityRequested = 0;
    private BigDecimal potentialRevenueOpportunity = BigDecimal.ZERO;
    private BigDecimal currentUtilizationPercent = BigDecimal.ZERO;
    private String statusTag = "HIGH DEMAND / FLEET CONSTRAINT";

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getConflictIncidentsCount() { return conflictIncidentsCount; }
    public void setConflictIncidentsCount(int conflictIncidentsCount) { this.conflictIncidentsCount = conflictIncidentsCount; }

    public int getUnmetQuantityRequested() { return unmetQuantityRequested; }
    public void setUnmetQuantityRequested(int unmetQuantityRequested) { this.unmetQuantityRequested = unmetQuantityRequested; }

    public BigDecimal getPotentialRevenueOpportunity() { return potentialRevenueOpportunity; }
    public void setPotentialRevenueOpportunity(BigDecimal potentialRevenueOpportunity) { this.potentialRevenueOpportunity = potentialRevenueOpportunity; }

    public BigDecimal getCurrentUtilizationPercent() { return currentUtilizationPercent; }
    public void setCurrentUtilizationPercent(BigDecimal currentUtilizationPercent) { this.currentUtilizationPercent = currentUtilizationPercent; }

    public String getStatusTag() { return statusTag; }
    public void setStatusTag(String statusTag) { this.statusTag = statusTag; }
}
