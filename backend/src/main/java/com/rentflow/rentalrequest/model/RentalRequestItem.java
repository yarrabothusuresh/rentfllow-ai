package com.rentflow.rentalrequest.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rental_request_items", indexes = {
    @Index(name = "idx_rri_request", columnList = "rentalRequestId"),
    @Index(name = "idx_rri_product", columnList = "productId")
})
public class RentalRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID rentalRequestId;

    @Column(nullable = false)
    private UUID productId;

    private String productName;
    private String sku;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public RentalRequestItem() {}

    public RentalRequestItem(UUID productId, String productName, String sku, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(UUID rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (this.unitPrice != null) {
            this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        if (unitPrice != null) {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
