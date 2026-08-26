package com.rentflow.returns.dto;

import java.util.List;

public class InspectionDashboardDTO {

    private long pendingInspection;
    private long goodItems;
    private long damagedItems;
    private long missingItems;
    private long maintenanceRequired;

    private List<ReturnOrderDTO> pendingInspectionList;

    public long getPendingInspection() { return pendingInspection; }
    public void setPendingInspection(long pendingInspection) { this.pendingInspection = pendingInspection; }

    public long getGoodItems() { return goodItems; }
    public void setGoodItems(long goodItems) { this.goodItems = goodItems; }

    public long getDamagedItems() { return damagedItems; }
    public void setDamagedItems(long damagedItems) { this.damagedItems = damagedItems; }

    public long getMissingItems() { return missingItems; }
    public void setMissingItems(long missingItems) { this.missingItems = missingItems; }

    public long getMaintenanceRequired() { return maintenanceRequired; }
    public void setMaintenanceRequired(long maintenanceRequired) { this.maintenanceRequired = maintenanceRequired; }

    public List<ReturnOrderDTO> getPendingInspectionList() { return pendingInspectionList; }
    public void setPendingInspectionList(List<ReturnOrderDTO> pendingInspectionList) { this.pendingInspectionList = pendingInspectionList; }
}
