package com.rentflow.ai.dto;

import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.model.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDTO {
    private UUID id;
    private String tenantId;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private UUID categoryId;
    private String categoryName;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private ProductType productType;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private ProductStatus status;

    @PositiveOrZero(message = "Rental price must be zero or positive")
    private BigDecimal rentalPrice;

    @PositiveOrZero(message = "Replacement cost must be zero or positive")
    private BigDecimal replacementCost; // Null for CUSTOMER role

    @PositiveOrZero(message = "Quantity owned must be zero or positive")
    private int quantityOwned;

    @PositiveOrZero(message = "Quantity in maintenance must be zero or positive")
    private int quantityInMaintenance;

    @PositiveOrZero(message = "Quantity damaged must be zero or positive")
    private int quantityDamaged;

    @PositiveOrZero(message = "Quantity lost must be zero or positive")
    private int quantityLost;

    private int availableQuantity; // Calculated
    private int quantityReserved; // Calculated for default period
    private String health; // GOOD, WARNING, CRITICAL

    @Size(max = 1000, message = "Image URL cannot exceed 1000 characters")
    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public BigDecimal getRentalPrice() { return rentalPrice; }
    public void setRentalPrice(BigDecimal rentalPrice) { this.rentalPrice = rentalPrice; }

    public BigDecimal getReplacementCost() { return replacementCost; }
    public void setReplacementCost(BigDecimal replacementCost) { this.replacementCost = replacementCost; }

    public int getQuantityOwned() { return quantityOwned; }
    public void setQuantityOwned(int quantityOwned) { this.quantityOwned = quantityOwned; }

    public int getQuantityInMaintenance() { return quantityInMaintenance; }
    public void setQuantityInMaintenance(int quantityInMaintenance) { this.quantityInMaintenance = quantityInMaintenance; }

    public int getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(int quantityDamaged) { this.quantityDamaged = quantityDamaged; }

    public int getQuantityLost() { return quantityLost; }
    public void setQuantityLost(int quantityLost) { this.quantityLost = quantityLost; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public int getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }

    public String getHealth() { return health; }
    public void setHealth(String health) { this.health = health; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
