package com.rentflow.aisales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RentalInquiryDTO {
    private UUID id;
    private UUID conversationId;
    private UUID customerId;
    private String eventType;
    private String eventName;
    private LocalDate eventDate;
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryTime;
    private boolean deliveryRequired;
    private Integer guestCount;
    private String tablePreference;
    private String chairPreference;
    private BigDecimal budget;
    private List<String> missingFields;
    private boolean complete;
    private String notes;

    public RentalInquiryDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalDateTime getRentalStart() { return rentalStart; }
    public void setRentalStart(LocalDateTime rentalStart) { this.rentalStart = rentalStart; }

    public LocalDateTime getRentalEnd() { return rentalEnd; }
    public void setRentalEnd(LocalDateTime rentalEnd) { this.rentalEnd = rentalEnd; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public boolean isDeliveryRequired() { return deliveryRequired; }
    public void setDeliveryRequired(boolean deliveryRequired) { this.deliveryRequired = deliveryRequired; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public String getTablePreference() { return tablePreference; }
    public void setTablePreference(String tablePreference) { this.tablePreference = tablePreference; }

    public String getChairPreference() { return chairPreference; }
    public void setChairPreference(String chairPreference) { this.chairPreference = chairPreference; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
