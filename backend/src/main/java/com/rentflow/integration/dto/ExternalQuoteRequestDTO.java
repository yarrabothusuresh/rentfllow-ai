package com.rentflow.integration.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ExternalQuoteRequestDTO {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String companyName;
    private String eventName;
    private String eventType;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private String venueAddress;
    private String notes;
    private List<ExternalQuoteRequestItemDTO> items;

    public ExternalQuoteRequestDTO() {}

    public static class ExternalQuoteRequestItemDTO {
        private UUID productId;
        private int quantity;

        public ExternalQuoteRequestItemDTO() {}

        public ExternalQuoteRequestItemDTO(UUID productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(LocalDate eventStartDate) { this.eventStartDate = eventStartDate; }

    public LocalDate getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(LocalDate eventEndDate) { this.eventEndDate = eventEndDate; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<ExternalQuoteRequestItemDTO> getItems() { return items; }
    public void setItems(List<ExternalQuoteRequestItemDTO> items) { this.items = items; }
}
