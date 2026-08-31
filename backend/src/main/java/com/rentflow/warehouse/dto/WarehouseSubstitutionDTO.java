package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.SubstitutionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WarehouseSubstitutionDTO {
    private UUID id;
    private String tenantId;
    private UUID warehouseOrderId;
    private String warehouseOrderNumber;
    private UUID bookingId;
    private String bookingNumber;
    private UUID originalProductId;
    private String originalProductName;
    private String originalProductSku;
    private BigDecimal originalPrice;
    private UUID replacementProductId;
    private String replacementProductName;
    private String replacementProductSku;
    private BigDecimal replacementPrice;
    private BigDecimal priceDifference;
    private int originalQuantity;
    private int replacementQuantity;
    private int replacementAvailableStock;
    private SubstitutionStatus status;
    private String reason;
    private String proposedBy;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWarehouseOrderId() { return warehouseOrderId; }
    public void setWarehouseOrderId(UUID warehouseOrderId) { this.warehouseOrderId = warehouseOrderId; }

    public String getWarehouseOrderNumber() { return warehouseOrderNumber; }
    public void setWarehouseOrderNumber(String warehouseOrderNumber) { this.warehouseOrderNumber = warehouseOrderNumber; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public UUID getOriginalProductId() { return originalProductId; }
    public void setOriginalProductId(UUID originalProductId) { this.originalProductId = originalProductId; }

    public String getOriginalProductName() { return originalProductName; }
    public void setOriginalProductName(String originalProductName) { this.originalProductName = originalProductName; }

    public String getOriginalProductSku() { return originalProductSku; }
    public void setOriginalProductSku(String originalProductSku) { this.originalProductSku = originalProductSku; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public UUID getReplacementProductId() { return replacementProductId; }
    public void setReplacementProductId(UUID replacementProductId) { this.replacementProductId = replacementProductId; }

    public String getReplacementProductName() { return replacementProductName; }
    public void setReplacementProductName(String replacementProductName) { this.replacementProductName = replacementProductName; }

    public String getReplacementProductSku() { return replacementProductSku; }
    public void setReplacementProductSku(String replacementProductSku) { this.replacementProductSku = replacementProductSku; }

    public BigDecimal getReplacementPrice() { return replacementPrice; }
    public void setReplacementPrice(BigDecimal replacementPrice) { this.replacementPrice = replacementPrice; }

    public BigDecimal getPriceDifference() { return priceDifference; }
    public void setPriceDifference(BigDecimal priceDifference) { this.priceDifference = priceDifference; }

    public int getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(int originalQuantity) { this.originalQuantity = originalQuantity; }

    public int getReplacementQuantity() { return replacementQuantity; }
    public void setReplacementQuantity(int replacementQuantity) { this.replacementQuantity = replacementQuantity; }

    public int getReplacementAvailableStock() { return replacementAvailableStock; }
    public void setReplacementAvailableStock(int replacementAvailableStock) { this.replacementAvailableStock = replacementAvailableStock; }

    public SubstitutionStatus getStatus() { return status; }
    public void setStatus(SubstitutionStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getProposedBy() { return proposedBy; }
    public void setProposedBy(String proposedBy) { this.proposedBy = proposedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
