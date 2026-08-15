package com.rentflow.ai.dto;

import com.rentflow.ai.model.LeadSource;
import com.rentflow.ai.model.LeadStatus;
import com.rentflow.ai.model.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LeadDTO {
    private UUID id;
    private String tenantId;
    private String firstName;
    private String lastName;
    private String companyName;
    private String email;
    private String phone;
    private LeadSource source;
    private EventType eventType;
    private LocalDate eventDate;
    private Integer guestCount;
    private String venueName;
    private String notes;
    private LeadStatus status;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LeadDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LeadSource getSource() { return source; }
    public void setSource(LeadSource source) { this.source = source; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
