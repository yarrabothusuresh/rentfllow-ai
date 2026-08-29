package com.rentflow.calendar.dto;

public class CalendarDashboardDTO {
    private long totalBookingsToday;
    private long totalDeliveriesToday;
    private long totalPickupsToday;
    private long totalReturnsToday;
    private long totalMaintenanceToday;
    private long activeDrivers;
    private long activeVehicles;
    private long openConflictsCount;
    private long capacityWarningsCount;

    public CalendarDashboardDTO() {}

    public long getTotalBookingsToday() { return totalBookingsToday; }
    public void setTotalBookingsToday(long totalBookingsToday) { this.totalBookingsToday = totalBookingsToday; }

    public long getTotalDeliveriesToday() { return totalDeliveriesToday; }
    public void setTotalDeliveriesToday(long totalDeliveriesToday) { this.totalDeliveriesToday = totalDeliveriesToday; }

    public long getTotalPickupsToday() { return totalPickupsToday; }
    public void setTotalPickupsToday(long totalPickupsToday) { this.totalPickupsToday = totalPickupsToday; }

    public long getTotalReturnsToday() { return totalReturnsToday; }
    public void setTotalReturnsToday(long totalReturnsToday) { this.totalReturnsToday = totalReturnsToday; }

    public long getTotalMaintenanceToday() { return totalMaintenanceToday; }
    public void setTotalMaintenanceToday(long totalMaintenanceToday) { this.totalMaintenanceToday = totalMaintenanceToday; }

    public long getActiveDrivers() { return activeDrivers; }
    public void setActiveDrivers(long activeDrivers) { this.activeDrivers = activeDrivers; }

    public long getActiveVehicles() { return activeVehicles; }
    public void setActiveVehicles(long activeVehicles) { this.activeVehicles = activeVehicles; }

    public long getOpenConflictsCount() { return openConflictsCount; }
    public void setOpenConflictsCount(long openConflictsCount) { this.openConflictsCount = openConflictsCount; }

    public long getCapacityWarningsCount() { return capacityWarningsCount; }
    public void setCapacityWarningsCount(long capacityWarningsCount) { this.capacityWarningsCount = capacityWarningsCount; }
}
