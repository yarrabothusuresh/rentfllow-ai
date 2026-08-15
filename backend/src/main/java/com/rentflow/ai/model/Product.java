package com.rentflow.ai.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_tenant", columnList = "tenantId"),
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_category", columnList = "categoryId"),
    @Index(name = "idx_product_status", columnList = "status")
})
public class Product {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType = ProductType.RENTAL_ITEM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(precision = 10, scale = 2)
    private BigDecimal rentalPrice = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal replacementCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private int quantityOwned = 0;

    @Column(nullable = false)
    private int quantityInMaintenance = 0;

    @Column(nullable = false)
    private int quantityDamaged = 0;

    @Column(nullable = false)
    private int quantityLost = 0;

    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Product(UUID id, String tenantId, String sku, String name, String description, UUID categoryId,
                   ProductType productType, ProductStatus status, BigDecimal rentalPrice, BigDecimal replacementCost,
                   int quantityOwned, int quantityInMaintenance, int quantityDamaged, int quantityLost) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.productType = productType != null ? productType : ProductType.RENTAL_ITEM;
        this.status = status != null ? status : ProductStatus.ACTIVE;
        this.rentalPrice = rentalPrice != null ? rentalPrice : BigDecimal.ZERO;
        this.replacementCost = replacementCost != null ? replacementCost : BigDecimal.ZERO;
        this.quantityOwned = quantityOwned;
        this.quantityInMaintenance = quantityInMaintenance;
        this.quantityDamaged = quantityDamaged;
        this.quantityLost = quantityLost;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
