package com.rentflow.aisales.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_rental_inquiries", indexes = {
    @Index(name = "idx_inquiry_tenant", columnList = "tenantId"),
    @Index(name = "idx_inquiry_conv", columnList = "conversationId"),
    @Index(name = "idx_inquiry_cust", columnList = "customerId")
})
public class RentalInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID conversationId;

    private UUID customerId;
    private UUID leadId;
    private UUID rentalRequestId;
    private UUID quoteId;

    private String customerName;
    private String companyName;
    private String email;
    private String phone;

    private String eventType;
    private String eventName;
    private LocalDate eventDate;
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;

    private String venueName;
    private String venueAddress;
    private String city;

    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryTime;
    private boolean deliveryRequired = true;
    private boolean pickupRequired = false;
    private boolean setupRequired = false;

    private Integer guestCount;
    private String tablePreference;
    private String chairPreference;
    private String productPreferences;
    private String productRequirements;
    private String stylePreferences;

    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(length = 2000)
    private String requestedItemsJson;

    @Column(length = 1000)
    private String missingFields;

    @Column(length = 2000)
    private String notes;

    private boolean availabilityChecked = false;
    private boolean estimateGenerated = false;
    private boolean leadCreated = false;
    private boolean rentalRequestCreated = false;
    private boolean quoteDraftCreated = false;

    @Column(nullable = false)
    private boolean complete = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

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

    public String getRequestedItemsJson() { return requestedItemsJson; }
    public void setRequestedItemsJson(String requestedItemsJson) { this.requestedItemsJson = requestedItemsJson; }

    public String getMissingFields() { return missingFields; }
    public void setMissingFields(String missingFields) { this.missingFields = missingFields; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }

    public UUID getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(UUID rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isPickupRequired() { return pickupRequired; }
    public void setPickupRequired(boolean pickupRequired) { this.pickupRequired = pickupRequired; }

    public boolean isSetupRequired() { return setupRequired; }
    public void setSetupRequired(boolean setupRequired) { this.setupRequired = setupRequired; }

    public String getProductPreferences() { return productPreferences; }
    public void setProductPreferences(String productPreferences) { this.productPreferences = productPreferences; }

    public String getProductRequirements() { return productRequirements; }
    public void setProductRequirements(String productRequirements) { this.productRequirements = productRequirements; }

    public String getStylePreferences() { return stylePreferences; }
    public void setStylePreferences(String stylePreferences) { this.stylePreferences = stylePreferences; }

    public boolean isAvailabilityChecked() { return availabilityChecked; }
    public void setAvailabilityChecked(boolean availabilityChecked) { this.availabilityChecked = availabilityChecked; }

    public boolean isEstimateGenerated() { return estimateGenerated; }
    public void setEstimateGenerated(boolean estimateGenerated) { this.estimateGenerated = estimateGenerated; }

    public boolean isLeadCreated() { return leadCreated; }
    public void setLeadCreated(boolean leadCreated) { this.leadCreated = leadCreated; }

    public boolean isRentalRequestCreated() { return rentalRequestCreated; }
    public void setRentalRequestCreated(boolean rentalRequestCreated) { this.rentalRequestCreated = rentalRequestCreated; }

    public boolean isQuoteDraftCreated() { return quoteDraftCreated; }
    public void setQuoteDraftCreated(boolean quoteDraftCreated) { this.quoteDraftCreated = quoteDraftCreated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
