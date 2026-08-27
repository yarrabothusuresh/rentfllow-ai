package com.rentflow.claims.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "replacement_orders", indexes = {
    @Index(name = "idx_replacement_tenant", columnList = "tenantId"),
    @Index(name = "idx_replacement_number", columnList = "replacementNumber"),
    @Index(name = "idx_replacement_claim", columnList = "claimId"),
    @Index(name = "idx_replacement_product", columnList = "productId"),
    @Index(name = "idx_replacement_status", columnList = "status")
})
public class ReplacementOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String replacementNumber;

    @Column(nullable = false)
    private UUID claimId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReplacementOrderStatus status = ReplacementOrderStatus.PENDING;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(length = 2000)
    private String reason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getReplacementNumber() { return replacementNumber; }
    public void setReplacementNumber(String replacementNumber) { this.replacementNumber = replacementNumber; }

    public UUID getClaimId() { return claimId; }
    public void setClaimId(UUID claimId) { this.claimId = claimId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public ReplacementOrderStatus getStatus() { return status; }
    public void setStatus(ReplacementOrderStatus status) { this.status = status; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
