package com.rentflow.crm.dto;

import com.rentflow.ai.model.EventType;
import com.rentflow.crm.model.LeadLostReason;
import com.rentflow.crm.model.LeadPriority;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.model.LeadStage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LeadDetailDTO {
    private UUID id;
    private String tenantId;
    private String leadNumber;
    private LeadSource source;
    private LeadStage stage;
    private LeadPriority priority;

    private String firstName;
    private String lastName;
    private String contactName;
    private String companyName;
    private String email;
    private String phone;
    private String preferredContactMethod;

    private String eventName;
    private EventType eventType;
    private LocalDate eventDate;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private String venueName;
    private String venueAddressSnapshot;
    private Integer guestCount;

    private BigDecimal estimatedBudget;
    private BigDecimal estimatedValue;

    private String customerNotes;
    private String internalNotes;

    private String assignedSalesUserId;
    private String assignedSalesUserName;

    private UUID rentalRequestId;
    private UUID customerId;
    private String customerName;
    private UUID quoteId;
    private String quoteNumber;
    private UUID bookingId;
    private String bookingNumber;

    private LeadLostReason lostReason;
    private String lostReasonNotes;
    private String reopenReason;

    private LocalDateTime nextFollowUpAt;
    private LocalDateTime lastContactedAt;
    private LocalDateTime qualifiedAt;
    private LocalDateTime convertedAt;
    private LocalDateTime wonAt;
    private LocalDateTime lostAt;
    private LocalDateTime reopenedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public LeadDetailDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getLeadNumber() { return leadNumber; }
    public void setLeadNumber(String leadNumber) { this.leadNumber = leadNumber; }

    public LeadSource getSource() { return source; }
    public void setSource(LeadSource source) { this.source = source; }

    public LeadStage getStage() { return stage; }
    public void setStage(LeadStage stage) { this.stage = stage; }

    public LeadPriority getPriority() { return priority; }
    public void setPriority(LeadPriority priority) { this.priority = priority; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalDate getRentalStartDate() { return rentalStartDate; }
    public void setRentalStartDate(LocalDate rentalStartDate) { this.rentalStartDate = rentalStartDate; }

    public LocalDate getRentalEndDate() { return rentalEndDate; }
    public void setRentalEndDate(LocalDate rentalEndDate) { this.rentalEndDate = rentalEndDate; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddressSnapshot() { return venueAddressSnapshot; }
    public void setVenueAddressSnapshot(String venueAddressSnapshot) { this.venueAddressSnapshot = venueAddressSnapshot; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public BigDecimal getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(BigDecimal estimatedBudget) { this.estimatedBudget = estimatedBudget; }

    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public String getAssignedSalesUserId() { return assignedSalesUserId; }
    public void setAssignedSalesUserId(String assignedSalesUserId) { this.assignedSalesUserId = assignedSalesUserId; }

    public String getAssignedSalesUserName() { return assignedSalesUserName; }
    public void setAssignedSalesUserName(String assignedSalesUserName) { this.assignedSalesUserName = assignedSalesUserName; }

    public UUID getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(UUID rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public LeadLostReason getLostReason() { return lostReason; }
    public void setLostReason(LeadLostReason lostReason) { this.lostReason = lostReason; }

    public String getLostReasonNotes() { return lostReasonNotes; }
    public void setLostReasonNotes(String lostReasonNotes) { this.lostReasonNotes = lostReasonNotes; }

    public String getReopenReason() { return reopenReason; }
    public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }

    public LocalDateTime getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(LocalDateTime nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }

    public LocalDateTime getLastContactedAt() { return lastContactedAt; }
    public void setLastContactedAt(LocalDateTime lastContactedAt) { this.lastContactedAt = lastContactedAt; }

    public LocalDateTime getQualifiedAt() { return qualifiedAt; }
    public void setQualifiedAt(LocalDateTime qualifiedAt) { this.qualifiedAt = qualifiedAt; }

    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }

    public LocalDateTime getWonAt() { return wonAt; }
    public void setWonAt(LocalDateTime wonAt) { this.wonAt = wonAt; }

    public LocalDateTime getLostAt() { return lostAt; }
    public void setLostAt(LocalDateTime lostAt) { this.lostAt = lostAt; }

    public LocalDateTime getReopenedAt() { return reopenedAt; }
    public void setReopenedAt(LocalDateTime reopenedAt) { this.reopenedAt = reopenedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
