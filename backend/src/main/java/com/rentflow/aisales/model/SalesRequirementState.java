package com.rentflow.aisales.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SalesRequirementState {

    private UUID conversationId;
    private String eventType;
    private String eventName;
    private LocalDate eventDate;
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;
    private Integer guestCount;

    private String venueName;
    private String venueAddress;
    private String city;

    private boolean deliveryRequired = true;
    private String deliveryTime;
    private boolean pickupRequired = false;
    private boolean setupRequired = false;

    private String productPreferences;
    private String productRequirements;
    private String stylePreferences;
    private BigDecimal budget;

    private String customerName;
    private String companyName;
    private String email;
    private String phone;

    private boolean availabilityChecked = false;
    private boolean estimateGenerated = false;
    private boolean leadCreated = false;
    private boolean rentalRequestCreated = false;
    private boolean quoteDraftCreated = false;

    private List<String> missingFields = new ArrayList<>();

    public SalesRequirementState() {}

    public static SalesRequirementState fromInquiry(RentalInquiry inq) {
        SalesRequirementState s = new SalesRequirementState();
        if (inq == null) return s;
        s.setConversationId(inq.getConversationId());
        s.setEventType(inq.getEventType());
        s.setEventName(inq.getEventName());
        s.setEventDate(inq.getEventDate());
        s.setRentalStart(inq.getRentalStart());
        s.setRentalEnd(inq.getRentalEnd());
        s.setGuestCount(inq.getGuestCount());
        s.setVenueName(inq.getVenueName());
        s.setVenueAddress(inq.getVenueAddress() != null ? inq.getVenueAddress() : inq.getDeliveryAddress());
        s.setCity(inq.getCity() != null ? inq.getCity() : inq.getDeliveryCity());
        s.setDeliveryRequired(inq.isDeliveryRequired());
        s.setDeliveryTime(inq.getDeliveryTime());
        s.setPickupRequired(inq.isPickupRequired());
        s.setSetupRequired(inq.isSetupRequired());
        s.setProductPreferences(inq.getProductPreferences() != null ? inq.getProductPreferences() : inq.getTablePreference());
        s.setProductRequirements(inq.getProductRequirements());
        s.setStylePreferences(inq.getStylePreferences() != null ? inq.getStylePreferences() : inq.getChairPreference());
        s.setBudget(inq.getBudget());
        s.setCustomerName(inq.getCustomerName());
        s.setCompanyName(inq.getCompanyName());
        s.setEmail(inq.getEmail());
        s.setPhone(inq.getPhone());
        s.setAvailabilityChecked(inq.isAvailabilityChecked());
        s.setEstimateGenerated(inq.isEstimateGenerated());
        s.setLeadCreated(inq.isLeadCreated());
        s.setRentalRequestCreated(inq.isRentalRequestCreated());
        s.setQuoteDraftCreated(inq.isQuoteDraftCreated());

        s.calculateMissingFields();
        return s;
    }

    public void applyToInquiry(RentalInquiry inq) {
        if (inq == null) return;
        if (this.eventType != null) inq.setEventType(this.eventType);
        if (this.eventName != null) inq.setEventName(this.eventName);
        if (this.eventDate != null) inq.setEventDate(this.eventDate);
        if (this.rentalStart != null) inq.setRentalStart(this.rentalStart);
        if (this.rentalEnd != null) inq.setRentalEnd(this.rentalEnd);
        if (this.guestCount != null) inq.setGuestCount(this.guestCount);
        if (this.venueName != null) inq.setVenueName(this.venueName);
        if (this.venueAddress != null) inq.setVenueAddress(this.venueAddress);
        if (this.city != null) {
            inq.setCity(this.city);
            inq.setDeliveryCity(this.city);
        }
        inq.setDeliveryRequired(this.deliveryRequired);
        if (this.deliveryTime != null) inq.setDeliveryTime(this.deliveryTime);
        inq.setPickupRequired(this.pickupRequired);
        inq.setSetupRequired(this.setupRequired);
        if (this.productPreferences != null) inq.setProductPreferences(this.productPreferences);
        if (this.productRequirements != null) inq.setProductRequirements(this.productRequirements);
        if (this.stylePreferences != null) inq.setStylePreferences(this.stylePreferences);
        if (this.budget != null) inq.setBudget(this.budget);
        if (this.customerName != null) inq.setCustomerName(this.customerName);
        if (this.companyName != null) inq.setCompanyName(this.companyName);
        if (this.email != null) inq.setEmail(this.email);
        if (this.phone != null) inq.setPhone(this.phone);
        inq.setAvailabilityChecked(this.availabilityChecked);
        inq.setEstimateGenerated(this.estimateGenerated);
        inq.setLeadCreated(this.leadCreated);
        inq.setRentalRequestCreated(this.rentalRequestCreated);
        inq.setQuoteDraftCreated(this.quoteDraftCreated);
    }

    public List<String> calculateMissingFields() {
        missingFields.clear();
        if (eventType == null || eventType.isBlank()) missingFields.add("eventType");
        if (eventDate == null && rentalStart == null) missingFields.add("eventDate");
        if (guestCount == null || guestCount <= 0) missingFields.add("guestCount");
        if (city == null || city.isBlank()) missingFields.add("city");
        if (email == null || email.isBlank()) missingFields.add("email");
        return missingFields;
    }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

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

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isDeliveryRequired() { return deliveryRequired; }
    public void setDeliveryRequired(boolean deliveryRequired) { this.deliveryRequired = deliveryRequired; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

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

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

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

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
}
