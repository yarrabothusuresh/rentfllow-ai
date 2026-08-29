package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_sm_tenant", columnList = "tenantId"),
    @Index(name = "idx_sm_product", columnList = "productId"),
    @Index(name = "idx_sm_item", columnList = "inventoryItemId"),
    @Index(name = "idx_sm_warehouse", columnList = "warehouseId"),
    @Index(name = "idx_sm_type", columnList = "movementType"),
    @Index(name = "idx_sm_created", columnList = "createdAt")
})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID productId;

    private UUID inventoryItemId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private int quantity;

    private UUID fromWarehouseId;
    private UUID toWarehouseId;

    private String referenceType;
    private UUID referenceId;

    private String reason;
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public StockMovement() {}

    public StockMovement(UUID id, String tenantId, UUID productId, UUID inventoryItemId, UUID warehouseId,
                         MovementType movementType, int quantity, UUID fromWarehouseId, UUID toWarehouseId,
                         String referenceType, UUID referenceId, String reason, String performedBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.inventoryItemId = inventoryItemId;
        this.warehouseId = warehouseId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.performedBy = performedBy != null ? performedBy : "System";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public UUID getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(UUID fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public UUID getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(UUID toWarehouseId) { this.toWarehouseId = toWarehouseId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
