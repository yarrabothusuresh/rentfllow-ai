package com.rentflow.claims.model;

import com.rentflow.returns.model.ReturnPriority;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "damage_claims", indexes = {
    @Index(name = "idx_claim_tenant", columnList = "tenantId"),
    @Index(name = "idx_claim_number", columnList = "claimNumber"),
    @Index(name = "idx_claim_booking", columnList = "bookingId"),
    @Index(name = "idx_claim_return", columnList = "returnOrderId"),
    @Index(name = "idx_claim_customer", columnList = "customerId"),
    @Index(name = "idx_claim_status", columnList = "status")
})
public class DamageClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID returnOrderId;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType = ClaimType.DAMAGE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnPriority priority = ReturnPriority.NORMAL;

    @Column(length = 2000)
    private String description;

    private LocalDateTime reportedAt;
    private String reportedBy;

    private LocalDateTime assessedAt;
    private String assessedBy;

    private boolean customerVisible = true;

    @Column(length = 2000)
    private String customerNotes;

    @Column(length = 2000)
    private String internalNotes;

    @Column(length = 2000)
    private String disputeReason;

    private LocalDateTime disputedAt;
    private String disputedBy;

    @Column(length = 2000)
    private String waiveReason;

    private LocalDateTime waivedAt;
    private String waivedBy;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedTotalCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal approvedTotalCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalTotalCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    private ClaimResolution resolution;

    @Column(length = 2000)
    private String resolutionNotes;

    private LocalDateTime resolvedAt;
    private String resolvedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reportedAt == null) this.reportedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getReturnOrderId() { return returnOrderId; }
    public void setReturnOrderId(UUID returnOrderId) { this.returnOrderId = returnOrderId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public ClaimType getClaimType() { return claimType; }
    public void setClaimType(ClaimType claimType) { this.claimType = claimType; }

    public ReturnPriority getPriority() { return priority; }
    public void setPriority(ReturnPriority priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public LocalDateTime getAssessedAt() { return assessedAt; }
    public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }

    public String getAssessedBy() { return assessedBy; }
    public void setAssessedBy(String assessedBy) { this.assessedBy = assessedBy; }

    public boolean isCustomerVisible() { return customerVisible; }
    public void setCustomerVisible(boolean customerVisible) { this.customerVisible = customerVisible; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public String getDisputeReason() { return disputeReason; }
    public void setDisputeReason(String disputeReason) { this.disputeReason = disputeReason; }

    public LocalDateTime getDisputedAt() { return disputedAt; }
    public void setDisputedAt(LocalDateTime disputedAt) { this.disputedAt = disputedAt; }

    public String getDisputedBy() { return disputedBy; }
    public void setDisputedBy(String disputedBy) { this.disputedBy = disputedBy; }

    public String getWaiveReason() { return waiveReason; }
    public void setWaiveReason(String waiveReason) { this.waiveReason = waiveReason; }

    public LocalDateTime getWaivedAt() { return waivedAt; }
    public void setWaivedAt(LocalDateTime waivedAt) { this.waivedAt = waivedAt; }

    public String getWaivedBy() { return waivedBy; }
    public void setWaivedBy(String waivedBy) { this.waivedBy = waivedBy; }

    public BigDecimal getEstimatedTotalCost() { return estimatedTotalCost; }
    public void setEstimatedTotalCost(BigDecimal estimatedTotalCost) { this.estimatedTotalCost = estimatedTotalCost; }

    public BigDecimal getApprovedTotalCost() { return approvedTotalCost; }
    public void setApprovedTotalCost(BigDecimal approvedTotalCost) { this.approvedTotalCost = approvedTotalCost; }

    public BigDecimal getFinalTotalCost() { return finalTotalCost; }
    public void setFinalTotalCost(BigDecimal finalTotalCost) { this.finalTotalCost = finalTotalCost; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public ClaimResolution getResolution() { return resolution; }
    public void setResolution(ClaimResolution resolution) { this.resolution = resolution; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
