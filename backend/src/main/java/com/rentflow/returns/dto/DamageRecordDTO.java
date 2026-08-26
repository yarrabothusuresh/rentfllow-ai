package com.rentflow.returns.dto;

import com.rentflow.returns.model.DamageCategory;
import com.rentflow.returns.model.DamageSeverity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DamageRecordDTO {
    private UUID id;
    private String tenantId;
    private UUID inspectionId;
    private UUID returnItemId;
    private String returnNumber;
    private UUID productId;
    private String productName;
    private String productSku;
    private String customerName;
    private int quantity;
    private DamageCategory category;
    private DamageSeverity severity;
    private String description;
    private BigDecimal estimatedRepairCost;
    private BigDecimal estimatedReplacementCost;
    private String status;
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getInspectionId() { return inspectionId; }
    public void setInspectionId(UUID inspectionId) { this.inspectionId = inspectionId; }

    public UUID getReturnItemId() { return returnItemId; }
    public void setReturnItemId(UUID returnItemId) { this.returnItemId = returnItemId; }

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public DamageCategory getCategory() { return category; }
    public void setCategory(DamageCategory category) { this.category = category; }

    public DamageSeverity getSeverity() { return severity; }
    public void setSeverity(DamageSeverity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getEstimatedRepairCost() { return estimatedRepairCost; }
    public void setEstimatedRepairCost(BigDecimal estimatedRepairCost) { this.estimatedRepairCost = estimatedRepairCost; }

    public BigDecimal getEstimatedReplacementCost() { return estimatedReplacementCost; }
    public void setEstimatedReplacementCost(BigDecimal estimatedReplacementCost) { this.estimatedReplacementCost = estimatedReplacementCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
