package com.rentflow.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_activities", indexes = {
    @Index(name = "idx_activity_tenant", columnList = "tenantId"),
    @Index(name = "idx_activity_customer", columnList = "customerId")
})
public class CustomerActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerActivityType activityType;

    private String referenceType;
    private UUID referenceId;

    @Column(length = 2000)
    private String description;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public CustomerActivity() {}

    public CustomerActivity(String tenantId, UUID customerId, CustomerActivityType activityType, String referenceType, UUID referenceId, String description) {
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.activityType = activityType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public CustomerActivityType getActivityType() { return activityType; }
    public void setActivityType(CustomerActivityType activityType) { this.activityType = activityType; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
