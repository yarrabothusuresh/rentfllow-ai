package com.rentflow.workflow.model;

public enum WorkflowStage {
    INQUIRY("01 Inquiry", "Sales"),
    LEAD("02 Lead", "Sales"),
    QUOTE("03 Quote", "Sales"),
    BOOKING("04 Booking", "Sales"),
    INVENTORY("05 Inventory", "Warehouse"),
    WAREHOUSE("06 Warehouse", "Warehouse"),
    DELIVERY("07 Delivery", "Driver"),
    EVENT("08 Event", "Operations"),
    PICKUP("09 Pickup", "Driver"),
    RETURN("10 Return", "Warehouse"),
    PAYMENT("11 Payment", "Finance"),
    COMPLETED("12 Completed", "Management");

    private final String displayName;
    private final String defaultRole;

    WorkflowStage(String displayName, String defaultRole) {
        this.displayName = displayName;
        this.defaultRole = defaultRole;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultRole() {
        return defaultRole;
    }
}
