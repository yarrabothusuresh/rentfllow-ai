package com.rentflow.rentalrequest.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rental_requests", indexes = {
    @Index(name = "idx_rental_req_tenant", columnList = "tenantId"),
    @Index(name = "idx_rental_req_number", columnList = "tenantId, requestNumber", unique = true),
    @Index(name = "idx_rental_req_idempotency", columnList = "tenantId, idempotencyKey"),
    @Index(name = "idx_rental_req_status", columnList = "status"),
    @Index(name = "idx_rental_req_lead", columnList = "leadId"),
    @Index(name = "idx_rental_req_quote", columnList = "quoteId")
})
public class RentalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String requestNumber;

    @Column(length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalRequestStatus status = RentalRequestStatus.SUBMITTED;

    private UUID conversationId;
    private UUID leadId;
    private UUID customerId;
    private UUID quoteId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
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

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedTotal = BigDecimal.ZERO;

    @Column(length = 4000)
    private String notes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "rentalRequestId")
    private List<RentalRequestItem> items = new ArrayList<>();

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

    public List<RentalRequestItem> getItems() { return items; }
    public void setItems(List<RentalRequestItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
