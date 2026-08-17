package com.rentflow.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rentflow.ai.model.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuoteDTO {
    private UUID id;
    private String tenantId;
    private String quoteNumber;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private UUID eventId;
    private String eventName;
    private String venueName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private QuoteStatus status = QuoteStatus.DRAFT;

    private LocalDate quoteDate;
    private LocalDate validUntil;
    private LocalDateTime rentalStartDateTime;
    private LocalDateTime rentalEndDateTime;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal pickupFee = BigDecimal.ZERO;
    private BigDecimal setupFee = BigDecimal.ZERO;
    private BigDecimal breakdownFee = BigDecimal.ZERO;
    private BigDecimal serviceFee = BigDecimal.ZERO;
    private BigDecimal totalFees = BigDecimal.ZERO;
    private BigDecimal taxRate = new BigDecimal("8.25");
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal depositPercentage = new BigDecimal("30.00");
    private BigDecimal depositAmount = BigDecimal.ZERO;
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    private String notes;
    private String internalNotes; // Redacted for CUSTOMER role
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuoteItemDTO> items = new ArrayList<>();
    private List<QuoteDiscountDTO> discounts = new ArrayList<>();
    private List<QuoteFeeDTO> fees = new ArrayList<>();

    // Dynamic warning feedback
    private boolean hasAvailabilityShortage = false;
    private List<String> shortageWarnings = new ArrayList<>();

    public QuoteDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }

    public LocalDate getQuoteDate() { return quoteDate; }
    public void setQuoteDate(LocalDate quoteDate) { this.quoteDate = quoteDate; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public LocalDateTime getRentalStartDateTime() { return rentalStartDateTime; }
    public void setRentalStartDateTime(LocalDateTime rentalStartDateTime) { this.rentalStartDateTime = rentalStartDateTime; }

    public LocalDateTime getRentalEndDateTime() { return rentalEndDateTime; }
    public void setRentalEndDateTime(LocalDateTime rentalEndDateTime) { this.rentalEndDateTime = rentalEndDateTime; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getPickupFee() { return pickupFee; }
    public void setPickupFee(BigDecimal pickupFee) { this.pickupFee = pickupFee; }

    public BigDecimal getSetupFee() { return setupFee; }
    public void setSetupFee(BigDecimal setupFee) { this.setupFee = setupFee; }

    public BigDecimal getBreakdownFee() { return breakdownFee; }
    public void setBreakdownFee(BigDecimal breakdownFee) { this.breakdownFee = breakdownFee; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositPercentage() { return depositPercentage; }
    public void setDepositPercentage(BigDecimal depositPercentage) { this.depositPercentage = depositPercentage; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<QuoteItemDTO> getItems() { return items; }
    public void setItems(List<QuoteItemDTO> items) { this.items = items; }

    public List<QuoteDiscountDTO> getDiscounts() { return discounts; }
    public void setDiscounts(List<QuoteDiscountDTO> discounts) { this.discounts = discounts; }

    public List<QuoteFeeDTO> getFees() { return fees; }
    public void setFees(List<QuoteFeeDTO> fees) { this.fees = fees; }

    public boolean isHasAvailabilityShortage() { return hasAvailabilityShortage; }
    public void setHasAvailabilityShortage(boolean hasAvailabilityShortage) { this.hasAvailabilityShortage = hasAvailabilityShortage; }

    public List<String> getShortageWarnings() { return shortageWarnings; }
    public void setShortageWarnings(List<String> shortageWarnings) { this.shortageWarnings = shortageWarnings; }
}
