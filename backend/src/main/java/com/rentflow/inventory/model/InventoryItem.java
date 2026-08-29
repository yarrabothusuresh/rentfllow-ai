package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_item_tenant", columnList = "tenantId"),
    @Index(name = "idx_item_product", columnList = "productId"),
    @Index(name = "idx_item_warehouse", columnList = "warehouseId"),
    @Index(name = "idx_item_asset_code", columnList = "assetCode"),
    @Index(name = "idx_item_serial", columnList = "serialNumber"),
    @Index(name = "idx_item_barcode", columnList = "barcode"),
    @Index(name = "idx_item_status", columnList = "status")
})
public class InventoryItem {

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
    private String assetCode;

    private String serialNumber;
    private String barcode;
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition = AssetCondition.GOOD;

    private LocalDateTime acquisitionDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal purchaseCost = BigDecimal.ZERO;

    private String currentLocation;
    private UUID currentBookingId;
    private LocalDateTime lastCheckedAt;

    @Version
    private Long version = 0L;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryItem() {}

    public InventoryItem(UUID id, String tenantId, UUID productId, UUID warehouseId, String assetCode,
                         String serialNumber, String barcode, String qrCode, AssetStatus status,
                         AssetCondition condition, BigDecimal purchaseCost, String currentLocation) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.assetCode = assetCode;
        this.serialNumber = serialNumber;
        this.barcode = barcode;
        this.qrCode = qrCode;
        this.status = status != null ? status : AssetStatus.AVAILABLE;
        this.condition = condition != null ? condition : AssetCondition.GOOD;
        this.purchaseCost = purchaseCost != null ? purchaseCost : BigDecimal.ZERO;
        this.currentLocation = currentLocation;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastCheckedAt == null) lastCheckedAt = LocalDateTime.now();
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

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public AssetCondition getCondition() { return condition; }
    public void setCondition(AssetCondition condition) { this.condition = condition; }

    public LocalDateTime getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDateTime acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public BigDecimal getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public UUID getCurrentBookingId() { return currentBookingId; }
    public void setCurrentBookingId(UUID currentBookingId) { this.currentBookingId = currentBookingId; }

    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
