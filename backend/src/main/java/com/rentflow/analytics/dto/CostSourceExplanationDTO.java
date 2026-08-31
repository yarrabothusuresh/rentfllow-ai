package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class CostSourceExplanationDTO {
    private String costCategory;
    private BigDecimal amount = BigDecimal.ZERO;
    private String source; // e.g. "Product.replacementCost amortization", "RepairOrder #RO-001 actualCost"
    private String calculationType; // ACTUAL, ESTIMATED, NOT_CONFIGURED

    public CostSourceExplanationDTO() {}

    public CostSourceExplanationDTO(String costCategory, BigDecimal amount, String source, String calculationType) {
        this.costCategory = costCategory;
        this.amount = amount;
        this.source = source;
        this.calculationType = calculationType;
    }

    public String getCostCategory() { return costCategory; }
    public void setCostCategory(String costCategory) { this.costCategory = costCategory; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCalculationType() { return calculationType; }
    public void setCalculationType(String calculationType) { this.calculationType = calculationType; }
}
