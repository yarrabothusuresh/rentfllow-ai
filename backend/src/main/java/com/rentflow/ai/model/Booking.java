package com.rentflow.ai.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking", indexes = {
    @Index(name = "idx_booking_tenant", columnList = "tenantId"),
    @Index(name = "idx_booking_quote", columnList = "quoteId"),
    @Index(name = "idx_booking_customer", columnList = "customerId"),
    @Index(name = "idx_booking_event", columnList = "eventId"),
    @Index(name = "idx_booking_status", columnList = "status")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String bookingNumber;

    @Column(nullable = false)
    private UUID quoteId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalDateTime rentalStartDateTime;

    @Column(nullable = false)
    private LocalDateTime rentalEndDateTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal pickupFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal setupFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal breakdownFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal depositRequired = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal depositPaid = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String internalNotes;

    private String createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingDate == null) bookingDate = LocalDate.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (deliveryFee == null) deliveryFee = BigDecimal.ZERO;
        if (pickupFee == null) pickupFee = BigDecimal.ZERO;
        if (setupFee == null) setupFee = BigDecimal.ZERO;
        if (breakdownFee == null) breakdownFee = BigDecimal.ZERO;
        if (serviceFee == null) serviceFee = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (depositRequired == null) depositRequired = BigDecimal.ZERO;
        if (depositPaid == null) depositPaid = BigDecimal.ZERO;
        if (balanceDue == null) balanceDue = totalAmount.subtract(depositPaid).max(BigDecimal.ZERO);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (totalAmount != null && depositPaid != null) {
            balanceDue = totalAmount.subtract(depositPaid).max(BigDecimal.ZERO);
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

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

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositRequired() { return depositRequired; }
    public void setDepositRequired(BigDecimal depositRequired) { this.depositRequired = depositRequired; }

    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal depositPaid) { this.depositPaid = depositPaid; }

    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }

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
}
