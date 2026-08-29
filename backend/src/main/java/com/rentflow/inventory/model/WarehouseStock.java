package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouse_stock", indexes = {
    @Index(name = "idx_wh_stock_tenant", columnList = "tenantId"),
    @Index(name = "idx_wh_stock_product", columnList = "productId"),
    @Index(name = "idx_wh_stock_warehouse", columnList = "warehouseId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_wh_stock_product_warehouse", columnNames = {"tenantId", "productId", "warehouseId"})
})
public class WarehouseStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private int quantityOnHand = 0;

    @Column(nullable = false)
    private int quantityAvailable = 0;

    @Column(nullable = false)
    private int quantityReserved = 0;

    @Column(nullable = false)
    private int quantityInMaintenance = 0;

    @Column(nullable = false)
    private int quantityDamaged = 0;

    @Column(nullable = false)
    private int quantityLost = 0;

    @Column(nullable = false)
    private int minimumStockLevel = 0;

    @Version
    private Long version = 0L;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WarehouseStock() {}

    public WarehouseStock(UUID id, String tenantId, UUID productId, UUID warehouseId,
                          int quantityOnHand, int quantityAvailable, int minimumStockLevel) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantityOnHand = quantityOnHand;
        this.quantityAvailable = quantityAvailable;
        this.minimumStockLevel = minimumStockLevel;
    }

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

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public int getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    public int getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }

    public int getQuantityInMaintenance() { return quantityInMaintenance; }
    public void setQuantityInMaintenance(int quantityInMaintenance) { this.quantityInMaintenance = quantityInMaintenance; }

    public int getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(int quantityDamaged) { this.quantityDamaged = quantityDamaged; }

    public int getQuantityLost() { return quantityLost; }
    public void setQuantityLost(int quantityLost) { this.quantityLost = quantityLost; }

    public int getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(int minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
