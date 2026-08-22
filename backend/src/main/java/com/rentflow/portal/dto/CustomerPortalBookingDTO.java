package com.rentflow.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CustomerPortalBookingDTO {
    private UUID id;
    private String bookingNumber;
    private String eventName;
    private LocalDate bookingDate;
    private LocalDateTime rentalStartDateTime;
    private LocalDateTime rentalEndDateTime;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal fees;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositPaid;
    private BigDecimal balanceDue;
    private String status;
    private String venueName;
    private String venueAddress;
    private String notes;
    private List<CustomerPortalBookingItemDTO> items;

    public CustomerPortalBookingDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

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

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal depositPaid) { this.depositPaid = depositPaid; }

    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<CustomerPortalBookingItemDTO> getItems() { return items; }
    public void setItems(List<CustomerPortalBookingItemDTO> items) { this.items = items; }

    public static class CustomerPortalBookingItemDTO {
        private UUID id;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineSubtotal;

        public CustomerPortalBookingItemDTO() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getLineSubtotal() { return lineSubtotal; }
        public void setLineSubtotal(BigDecimal lineSubtotal) { this.lineSubtotal = lineSubtotal; }
    }
}
