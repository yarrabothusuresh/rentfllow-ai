package com.rentflow.rentalrequest.dto;

import com.rentflow.rentalrequest.model.RentalRequestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RentalRequestDTO {

    private UUID id;
    private String tenantId;
    private String requestNumber;
    private String idempotencyKey;
    private RentalRequestStatus status;
    private UUID conversationId;
    private UUID leadId;
    private UUID customerId;
    private UUID quoteId;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalDateTime rentalStartDate;
    private LocalDateTime rentalEndDate;

    private String deliveryAddress;
    private String deliveryCity;
    private boolean deliveryRequired = true;

    private Integer guestCount;
    private BigDecimal estimatedBudget;
    private BigDecimal estimatedTotal;
    private String notes;

    private List<ItemDTO> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class ItemDTO {
        private UUID id;
        private UUID productId;
        private String productName;
        private String sku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;

        public ItemDTO() {}

        public ItemDTO(UUID productId, String productName, String sku, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public RentalRequestStatus getStatus() { return status; }
    public void setStatus(RentalRequestStatus status) { this.status = status; }
    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalDateTime getRentalStartDate() { return rentalStartDate; }
    public void setRentalStartDate(LocalDateTime rentalStartDate) { this.rentalStartDate = rentalStartDate; }
    public LocalDateTime getRentalEndDate() { return rentalEndDate; }
    public void setRentalEndDate(LocalDateTime rentalEndDate) { this.rentalEndDate = rentalEndDate; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    public boolean isDeliveryRequired() { return deliveryRequired; }
    public void setDeliveryRequired(boolean deliveryRequired) { this.deliveryRequired = deliveryRequired; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public BigDecimal getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(BigDecimal estimatedBudget) { this.estimatedBudget = estimatedBudget; }
    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
