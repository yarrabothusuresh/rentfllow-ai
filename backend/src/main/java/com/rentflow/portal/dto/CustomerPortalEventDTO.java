package com.rentflow.portal.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CustomerPortalEventDTO {
    private UUID id;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private String startTime;
    private String endTime;
    private String venueName;
    private String venueAddress;
    private String city;
    private String state;
    private String zipCode;
    private String status;
    private List<String> requirements;

    public CustomerPortalEventDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }
}
