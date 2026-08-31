package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class UnderutilizedProductDTO {
    private UUID productId;
    private String sku;
    private String name;
    private String categoryName;
    private int quantityOwned = 0;
    private BigDecimal inventoryAssetValue = BigDecimal.ZERO;
    private BigDecimal utilizationPercent = BigDecimal.ZERO;
    private BigDecimal rentalRevenue = BigDecimal.ZERO;
    private LocalDate lastRentalDate;
    private String suggestedAction = "Evaluate promotional discounting or bundle packaging";

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getQuantityOwned() { return quantityOwned; }
    public void setQuantityOwned(int quantityOwned) { this.quantityOwned = quantityOwned; }

    public BigDecimal getInventoryAssetValue() { return inventoryAssetValue; }
    public void setInventoryAssetValue(BigDecimal inventoryAssetValue) { this.inventoryAssetValue = inventoryAssetValue; }

    public BigDecimal getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(BigDecimal utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public BigDecimal getRentalRevenue() { return rentalRevenue; }
    public void setRentalRevenue(BigDecimal rentalRevenue) { this.rentalRevenue = rentalRevenue; }

    public LocalDate getLastRentalDate() { return lastRentalDate; }
    public void setLastRentalDate(LocalDate lastRentalDate) { this.lastRentalDate = lastRentalDate; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
}
