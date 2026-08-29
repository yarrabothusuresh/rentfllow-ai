package com.rentflow.calendar.dto;

import com.rentflow.calendar.model.CalendarEventType;

import java.time.LocalDateTime;

public class CalendarEventDTO {
    private String id;
    private String tenantId;
    private CalendarEventType eventType;
    private String referenceType;
    private String referenceId;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String status;
    private String location;
    private String priority;
    private String customerName;
    private String driverName;
    private String vehicleName;
    private String notes;

    public CalendarEventDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public CalendarEventType getEventType() { return eventType; }
    public void setEventType(CalendarEventType eventType) { this.eventType = eventType; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
