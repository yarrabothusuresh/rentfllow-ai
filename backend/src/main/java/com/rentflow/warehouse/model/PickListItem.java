package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pick_list_items", indexes = {
    @Index(name = "idx_pick_item_tenant", columnList = "tenantId"),
    @Index(name = "idx_pick_item_list", columnList = "pickListId"),
    @Index(name = "idx_pick_item_product", columnList = "productId"),
    @Index(name = "idx_pick_item_asset", columnList = "inventoryItemId"),
    @Index(name = "idx_pick_item_status", columnList = "status"),
    @Index(name = "idx_pick_item_sequence", columnList = "sequenceNumber")
})
public class PickListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID pickListId;

    private UUID bookingItemId;
    private UUID productId;
    private UUID inventoryItemId;
    private UUID warehouseLocationId;

    private String productNameSnapshot;
    private String skuSnapshot;
    private String locationCodeSnapshot;

    @Column(nullable = false)
    private int requiredQuantity;

    @Column(nullable = false)
    private int pickedQuantity = 0;

    @Column(nullable = false)
    private int shortQuantity = 0;

    @Column(nullable = false)
    private int damagedQuantity = 0;

    @Column(nullable = false)
    private int substitutedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickListItemStatus status = PickListItemStatus.PENDING;

    @Column(nullable = false)
    private int sequenceNumber = 1;

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PickListItemStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getPickListId() { return pickListId; }
    public void setPickListId(UUID pickListId) { this.pickListId = pickListId; }

    public UUID getBookingItemId() { return bookingItemId; }
    public void setBookingItemId(UUID bookingItemId) { this.bookingItemId = bookingItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getWarehouseLocationId() { return warehouseLocationId; }
    public void setWarehouseLocationId(UUID warehouseLocationId) { this.warehouseLocationId = warehouseLocationId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public String getLocationCodeSnapshot() { return locationCodeSnapshot; }
    public void setLocationCodeSnapshot(String locationCodeSnapshot) { this.locationCodeSnapshot = locationCodeSnapshot; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getPickedQuantity() { return pickedQuantity; }
    public void setPickedQuantity(int pickedQuantity) { this.pickedQuantity = pickedQuantity; }

    public int getShortQuantity() { return shortQuantity; }
    public void setShortQuantity(int shortQuantity) { this.shortQuantity = shortQuantity; }

    public int getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }

    public int getSubstitutedQuantity() { return substitutedQuantity; }
    public void setSubstitutedQuantity(int substitutedQuantity) { this.substitutedQuantity = substitutedQuantity; }

    public PickListItemStatus getStatus() { return status; }
    public void setStatus(PickListItemStatus status) { this.status = status; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
