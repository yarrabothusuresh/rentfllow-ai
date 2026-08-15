package com.rentflow.ai.dto;

public class InventorySummaryDTO {
    private int totalProducts;
    private int totalUnits;
    private int availableUnits;
    private int reservedUnits;
    private int maintenanceUnits;
    private int damagedUnits;
    private int lostUnits;
    private int lowStockProducts;

    public InventorySummaryDTO() {}

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public int getTotalUnits() { return totalUnits; }
    public void setTotalUnits(int totalUnits) { this.totalUnits = totalUnits; }

    public int getAvailableUnits() { return availableUnits; }
    public void setAvailableUnits(int availableUnits) { this.availableUnits = availableUnits; }

    public int getReservedUnits() { return reservedUnits; }
    public void setReservedUnits(int reservedUnits) { this.reservedUnits = reservedUnits; }

    public int getMaintenanceUnits() { return maintenanceUnits; }
    public void setMaintenanceUnits(int maintenanceUnits) { this.maintenanceUnits = maintenanceUnits; }

    public int getDamagedUnits() { return damagedUnits; }
    public void setDamagedUnits(int damagedUnits) { this.damagedUnits = damagedUnits; }

    public int getLostUnits() { return lostUnits; }
    public void setLostUnits(int lostUnits) { this.lostUnits = lostUnits; }

    public int getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(int lowStockProducts) { this.lowStockProducts = lowStockProducts; }
}
