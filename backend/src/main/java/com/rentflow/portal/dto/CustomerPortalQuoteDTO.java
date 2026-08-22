package com.rentflow.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CustomerPortalQuoteDTO {
    private UUID id;
    private String quoteNumber;
    private String eventName;
    private LocalDate quoteDate;
    private LocalDate validUntil;
    private LocalDateTime rentalStartDateTime;
    private LocalDateTime rentalEndDateTime;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal fees;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositRequired;
    private String status;
    private String notes;
    private List<CustomerPortalQuoteItemDTO> items;

    public CustomerPortalQuoteDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

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

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositRequired() { return depositRequired; }
    public void setDepositRequired(BigDecimal depositRequired) { this.depositRequired = depositRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<CustomerPortalQuoteItemDTO> getItems() { return items; }
    public void setItems(List<CustomerPortalQuoteItemDTO> items) { this.items = items; }

    public static class CustomerPortalQuoteItemDTO {
        private UUID id;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineSubtotal;

        public CustomerPortalQuoteItemDTO() {}

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
