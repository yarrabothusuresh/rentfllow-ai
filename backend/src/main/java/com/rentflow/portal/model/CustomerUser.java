package com.rentflow.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_users", indexes = {
    @Index(name = "idx_customer_user_tenant", columnList = "tenantId"),
    @Index(name = "idx_customer_user_customer", columnList = "customerId"),
    @Index(name = "idx_customer_user_email", columnList = "tenantId, email")
})
public class CustomerUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public CustomerUser() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
