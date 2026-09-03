package com.rentflow.crm.dto;

import com.rentflow.ai.model.EventType;
import com.rentflow.crm.model.LeadPriority;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.model.LeadStage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LeadSummaryDTO {
    private UUID id;
    private String leadNumber;
    private LeadSource source;
    private LeadStage stage;
    private LeadPriority priority;
    private String contactName;
    private String companyName;
    private String email;
    private String phone;
    private String eventName;
    private EventType eventType;
    private LocalDate eventDate;
    private BigDecimal estimatedValue;
    private String assignedSalesUserId;
    private String assignedSalesUserName;
    private LocalDateTime nextFollowUpAt;
    private LocalDateTime lastContactedAt;
    private LocalDateTime createdAt;
    private boolean overdueFollowUp;

    public LeadSummaryDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLeadNumber() { return leadNumber; }
    public void setLeadNumber(String leadNumber) { this.leadNumber = leadNumber; }

    public LeadSource getSource() { return source; }
    public void setSource(LeadSource source) { this.source = source; }

    public LeadStage getStage() { return stage; }
    public void setStage(LeadStage stage) { this.stage = stage; }

    public LeadPriority getPriority() { return priority; }
    public void setPriority(LeadPriority priority) { this.priority = priority; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }

    public String getAssignedSalesUserId() { return assignedSalesUserId; }
    public void setAssignedSalesUserId(String assignedSalesUserId) { this.assignedSalesUserId = assignedSalesUserId; }

    public String getAssignedSalesUserName() { return assignedSalesUserName; }
    public void setAssignedSalesUserName(String assignedSalesUserName) { this.assignedSalesUserName = assignedSalesUserName; }

    public LocalDateTime getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(LocalDateTime nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }

    public LocalDateTime getLastContactedAt() { return lastContactedAt; }
    public void setLastContactedAt(LocalDateTime lastContactedAt) { this.lastContactedAt = lastContactedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isOverdueFollowUp() { return overdueFollowUp; }
    public void setOverdueFollowUp(boolean overdueFollowUp) { this.overdueFollowUp = overdueFollowUp; }
}
