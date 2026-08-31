package com.rentflow.warehouse.dto;

public class LoadHandoffRequestDTO {
    private String driverName;
    private String driverNotes;

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverNotes() { return driverNotes; }
    public void setDriverNotes(String driverNotes) { this.driverNotes = driverNotes; }
}
