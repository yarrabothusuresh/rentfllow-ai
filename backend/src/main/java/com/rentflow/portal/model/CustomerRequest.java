package com.rentflow.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_requests", indexes = {
    @Index(name = "idx_customer_req_tenant", columnList = "tenantId"),
    @Index(name = "idx_customer_req_customer", columnList = "customerId"),
    @Index(name = "idx_customer_req_status", columnList = "status")
})
public class CustomerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID customerId;

    private UUID quoteId;
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType = RequestType.GENERAL;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.OPEN;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = RequestStatus.OPEN;
        if (requestType == null) requestType = RequestType.GENERAL;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public CustomerRequest() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
