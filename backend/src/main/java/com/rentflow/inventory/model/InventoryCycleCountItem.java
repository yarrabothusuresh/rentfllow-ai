package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "inventory_cycle_count_items", indexes = {
    @Index(name = "idx_cci_count", columnList = "cycleCountId"),
    @Index(name = "idx_cci_product", columnList = "productId"),
    @Index(name = "idx_cci_item", columnList = "inventoryItemId")
})
public class InventoryCycleCountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID cycleCountId;

    @Column(nullable = false)
    private UUID productId;

    private UUID inventoryItemId;

    @Column(nullable = false)
    private int expectedQuantity = 0;

    @Column(nullable = false)
    private int countedQuantity = 0;

    @Column(nullable = false)
    private int variance = 0;

    private String reason;
    private String notes;

    public InventoryCycleCountItem() {}

    public InventoryCycleCountItem(UUID id, UUID cycleCountId, UUID productId, UUID inventoryItemId,
                                   int expectedQuantity, int countedQuantity, String reason, String notes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cycleCountId = cycleCountId;
        this.productId = productId;
        this.inventoryItemId = inventoryItemId;
        this.expectedQuantity = expectedQuantity;
        this.countedQuantity = countedQuantity;
        this.variance = countedQuantity - expectedQuantity;
        this.reason = reason;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCycleCountId() { return cycleCountId; }
    public void setCycleCountId(UUID cycleCountId) { this.cycleCountId = cycleCountId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public int getExpectedQuantity() { return expectedQuantity; }
    public void setExpectedQuantity(int expectedQuantity) { this.expectedQuantity = expectedQuantity; }

    public int getCountedQuantity() { return countedQuantity; }
    public void setCountedQuantity(int countedQuantity) {
        this.countedQuantity = countedQuantity;
        this.variance = this.countedQuantity - this.expectedQuantity;
    }

    public int getVariance() { return variance; }
    public void setVariance(int variance) { this.variance = variance; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
