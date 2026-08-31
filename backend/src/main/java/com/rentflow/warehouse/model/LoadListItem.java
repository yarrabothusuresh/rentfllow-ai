package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "load_list_items", indexes = {
    @Index(name = "idx_load_item_tenant", columnList = "tenantId"),
    @Index(name = "idx_load_item_list", columnList = "loadListId"),
    @Index(name = "idx_load_item_product", columnList = "productId"),
    @Index(name = "idx_load_item_container", columnList = "containerId"),
    @Index(name = "idx_load_item_status", columnList = "status")
})
public class LoadListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID loadListId;

    private UUID productId;
    private UUID inventoryItemId;
    private UUID containerId;

    private String productNameSnapshot;
    private String containerCodeSnapshot;

    @Column(nullable = false)
    private int requiredQuantity;

    @Column(nullable = false)
    private int loadedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadListItemStatus status = LoadListItemStatus.PENDING;

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = LoadListItemStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getLoadListId() { return loadListId; }
    public void setLoadListId(UUID loadListId) { this.loadListId = loadListId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getContainerId() { return containerId; }
    public void setContainerId(UUID containerId) { this.containerId = containerId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getContainerCodeSnapshot() { return containerCodeSnapshot; }
    public void setContainerCodeSnapshot(String containerCodeSnapshot) { this.containerCodeSnapshot = containerCodeSnapshot; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getLoadedQuantity() { return loadedQuantity; }
    public void setLoadedQuantity(int loadedQuantity) { this.loadedQuantity = loadedQuantity; }

    public LoadListItemStatus getStatus() { return status; }
    public void setStatus(LoadListItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
