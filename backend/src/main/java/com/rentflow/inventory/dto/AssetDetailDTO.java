package com.rentflow.inventory.dto;

import com.rentflow.inventory.model.AssetCondition;
import com.rentflow.inventory.model.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AssetDetailDTO {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private UUID warehouseId;
    private String warehouseName;
    private String assetCode;
    private String serialNumber;
    private String barcode;
    private String qrCode;
    private AssetStatus status;
    private AssetCondition condition;
    private LocalDateTime acquisitionDate;
    private BigDecimal purchaseCost;
    private String currentLocation;
    private UUID currentBookingId;
    private String currentBookingNumber;
    private String customerName;
    private LocalDateTime lastCheckedAt;
    private List<MovementHistoryDTO> history;

    public static class MovementHistoryDTO {
        private UUID id;
        private String movementType;
        private int quantity;
        private String fromWarehouseName;
        private String toWarehouseName;
        private String referenceType;
        private UUID referenceId;
        private String reason;
        private String performedBy;
        private LocalDateTime timestamp;

        public MovementHistoryDTO() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getFromWarehouseName() { return fromWarehouseName; }
        public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }

        public String getToWarehouseName() { return toWarehouseName; }
        public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }

        public String getReferenceType() { return referenceType; }
        public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

        public UUID getReferenceId() { return referenceId; }
        public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public AssetDetailDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

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

    public String getCurrentBookingNumber() { return currentBookingNumber; }
    public void setCurrentBookingNumber(String currentBookingNumber) { this.currentBookingNumber = currentBookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public List<MovementHistoryDTO> getHistory() { return history; }
    public void setHistory(List<MovementHistoryDTO> history) { this.history = history; }
}
