package com.rentflow.returns.dto;

import com.rentflow.returns.model.ReturnOrderItemStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReturnOrderItemDTO {
    private UUID id;
    private String tenantId;
    private UUID returnOrderId;
    private UUID bookingItemId;
    private UUID productId;
    private String productNameSnapshot;
    private String skuSnapshot;
    private int quantityExpected;
    private int quantityReceived;
    private int quantityMissing;
    private int quantityDamaged;
    private int quantityGood;
    private ReturnOrderItemStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
