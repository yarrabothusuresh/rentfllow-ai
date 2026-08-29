package com.rentflow.inventory.dto;

import com.rentflow.inventory.model.AssetCondition;
import com.rentflow.inventory.model.AssetStatus;

import java.util.UUID;

public class AssetScanResultDTO {
    private boolean found;
    private UUID assetId;
    private String assetCode;
    private String serialNumber;
    private String barcode;
    private UUID productId;
    private String productName;
    private String productSku;
    private UUID warehouseId;
    private String warehouseName;
    private AssetStatus status;
    private AssetCondition condition;
    private UUID currentBookingId;
    private String currentBookingNumber;
    private String customerName;
    private String message;

    public AssetScanResultDTO() {}

    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

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

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public AssetCondition getCondition() { return condition; }
    public void setCondition(AssetCondition condition) { this.condition = condition; }

    public UUID getCurrentBookingId() { return currentBookingId; }
    public void setCurrentBookingId(UUID currentBookingId) { this.currentBookingId = currentBookingId; }

    public String getCurrentBookingNumber() { return currentBookingNumber; }
    public void setCurrentBookingNumber(String currentBookingNumber) { this.currentBookingNumber = currentBookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
