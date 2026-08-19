package com.rentflow.payment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_audit", indexes = {
    @Index(name = "idx_payaudit_tenant", columnList = "tenantId"),
    @Index(name = "idx_payaudit_booking", columnList = "bookingId"),
    @Index(name = "idx_payaudit_payment", columnList = "paymentId")
})
public class PaymentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID bookingId;

    private UUID paymentId;

    @Column(nullable = false)
    private String action; // e.g. PAYMENT_RECORDED, PAYMENT_VOIDED

    private String performedBy;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public PaymentAudit() {}

    public PaymentAudit(String tenantId, UUID bookingId, UUID paymentId, String action, String performedBy, String details) {
        this.tenantId = tenantId;
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
