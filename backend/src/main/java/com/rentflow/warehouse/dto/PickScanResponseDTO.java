package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.PickListItemStatus;
import java.util.UUID;

public class PickScanResponseDTO {
    private boolean success;
    private String message;
    private UUID pickListItemId;
    private UUID productId;
    private String productName;
    private String productSku;
    private UUID assetId;
    private String assetCode;
    private String barcode;
    private String locationCode;
    private int requiredQuantity;
    private int pickedQuantity;
    private int remainingQuantity;
    private PickListItemStatus itemStatus;
    private double pickListProgressPercentage;
    private boolean pickListComplete;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UUID getPickListItemId() { return pickListItemId; }
    public void setPickListItemId(UUID pickListItemId) { this.pickListItemId = pickListItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getPickedQuantity() { return pickedQuantity; }
    public void setPickedQuantity(int pickedQuantity) { this.pickedQuantity = pickedQuantity; }

    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public PickListItemStatus getItemStatus() { return itemStatus; }
    public void setItemStatus(PickListItemStatus itemStatus) { this.itemStatus = itemStatus; }

    public double getPickListProgressPercentage() { return pickListProgressPercentage; }
    public void setPickListProgressPercentage(double pickListProgressPercentage) { this.pickListProgressPercentage = pickListProgressPercentage; }

    public boolean isPickListComplete() { return pickListComplete; }
    public void setPickListComplete(boolean pickListComplete) { this.pickListComplete = pickListComplete; }
}
