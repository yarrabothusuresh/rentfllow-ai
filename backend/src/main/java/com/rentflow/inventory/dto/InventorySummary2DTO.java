package com.rentflow.inventory.dto;

import java.math.BigDecimal;

public class InventorySummary2DTO {
    private long totalAssets;
    private long availableAssets;
    private long reservedAssets;
    private long outOnRentAssets;
    private long maintenanceAssets;
    private long damagedAssets;
    private long lostAssets;
    private long retiredAssets;
    private BigDecimal totalAssetValue = BigDecimal.ZERO;
    private long lowStockCount;
    private long itemsInTransit;
    private long stockAdjustmentsToday;

    public InventorySummary2DTO() {}

    public long getTotalAssets() { return totalAssets; }
    public void setTotalAssets(long totalAssets) { this.totalAssets = totalAssets; }

    public long getAvailableAssets() { return availableAssets; }
    public void setAvailableAssets(long availableAssets) { this.availableAssets = availableAssets; }

    public long getReservedAssets() { return reservedAssets; }
    public void setReservedAssets(long reservedAssets) { this.reservedAssets = reservedAssets; }

    public long getOutOnRentAssets() { return outOnRentAssets; }
    public void setOutOnRentAssets(long outOnRentAssets) { this.outOnRentAssets = outOnRentAssets; }

    public long getMaintenanceAssets() { return maintenanceAssets; }
    public void setMaintenanceAssets(long maintenanceAssets) { this.maintenanceAssets = maintenanceAssets; }

    public long getDamagedAssets() { return damagedAssets; }
    public void setDamagedAssets(long damagedAssets) { this.damagedAssets = damagedAssets; }

    public long getLostAssets() { return lostAssets; }
    public void setLostAssets(long lostAssets) { this.lostAssets = lostAssets; }

    public long getRetiredAssets() { return retiredAssets; }
    public void setRetiredAssets(long retiredAssets) { this.retiredAssets = retiredAssets; }

    public BigDecimal getTotalAssetValue() { return totalAssetValue; }
    public void setTotalAssetValue(BigDecimal totalAssetValue) { this.totalAssetValue = totalAssetValue; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getItemsInTransit() { return itemsInTransit; }
    public void setItemsInTransit(long itemsInTransit) { this.itemsInTransit = itemsInTransit; }

    public long getStockAdjustmentsToday() { return stockAdjustmentsToday; }
    public void setStockAdjustmentsToday(long stockAdjustmentsToday) { this.stockAdjustmentsToday = stockAdjustmentsToday; }
}
