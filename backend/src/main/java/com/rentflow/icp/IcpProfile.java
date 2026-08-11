package com.rentflow.icp;

public record IcpProfile(
    String name,
    String revenueRange,
    String employeeRange,
    String warehouseRange,
    String productRange,
    String market,
    String validationStatus
) {}
