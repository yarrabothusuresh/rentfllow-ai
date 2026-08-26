package com.rentflow.returns.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_order_items", indexes = {
    @Index(name = "idx_return_item_tenant", columnList = "tenantId"),
    @Index(name = "idx_return_item_order", columnList = "returnOrderId"),
    @Index(name = "idx_return_item_product", columnList = "productId")
})
public class ReturnOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID returnOrderId;

    private UUID bookingItemId;

    @Column(nullable = false)
    private UUID productId;

    private String productNameSnapshot;
    private String skuSnapshot;

    @Column(nullable = false)
    private int quantityExpected = 0;

    @Column(nullable = false)
    private int quantityReceived = 0;

    @Column(nullable = false)
    private int quantityMissing = 0;

    @Column(nullable = false)
    private int quantityDamaged = 0;

    @Column(nullable = false)
    private int quantityGood = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnOrderItemStatus status = ReturnOrderItemStatus.PENDING;

    @Column(length = 2000)
    private String notes;

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getReturnOrderId() { return returnOrderId; }
    public void setReturnOrderId(UUID returnOrderId) { this.returnOrderId = returnOrderId; }

    public UUID getBookingItemId() { return bookingItemId; }
    public void setBookingItemId(UUID bookingItemId) { this.bookingItemId = bookingItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public int getQuantityExpected() { return quantityExpected; }
    public void setQuantityExpected(int quantityExpected) { this.quantityExpected = quantityExpected; }

    public int getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }

    public int getQuantityMissing() { return quantityMissing; }
    public void setQuantityMissing(int quantityMissing) { this.quantityMissing = quantityMissing; }

    public int getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(int quantityDamaged) { this.quantityDamaged = quantityDamaged; }

    public int getQuantityGood() { return quantityGood; }
    public void setQuantityGood(int quantityGood) { this.quantityGood = quantityGood; }

    public ReturnOrderItemStatus getStatus() { return status; }
    public void setStatus(ReturnOrderItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
